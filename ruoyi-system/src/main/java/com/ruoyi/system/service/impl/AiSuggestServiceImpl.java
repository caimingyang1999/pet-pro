package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AiSuggestWord;
import com.ruoyi.system.domain.PetInfo;
import com.ruoyi.system.domain.dto.AiSuggestWordDTO;
import com.ruoyi.system.domain.vo.SuggestWordVO;
import com.ruoyi.system.mapper.AiSuggestWordMapper;
import com.ruoyi.system.mapper.PetInfoMapper;
import com.ruoyi.system.service.IAiSuggestService;

/**
 * AI搜索推荐词 服务实现
 * 根据用户已添加宠物信息智能推荐搜索提示词：
 * 1. 统计用户宠物类型分布
 * 2. 按权重分配每种类型的推荐数量
 * 3. 从词库中按权重取词，并叠加类型匹配系数与随机扰动得到最终得分
 *
 * @author ruoyi
 */
@Service
public class AiSuggestServiceImpl extends ServiceImpl<AiSuggestWordMapper, AiSuggestWord> implements IAiSuggestService
{
    @Resource
    private AiSuggestWordMapper aiSuggestWordMapper;

    @Resource
    private PetInfoMapper petInfoMapper;

    /** 默认返回数量 */
    private static final int DEFAULT_LIMIT = 4;

    /** 通用类型在分配中的固定名额 */
    private static final int GENERAL_SLOTS = 2;

    /** 每种宠物类型的最低保底名额 */
    private static final int MIN_PER_TYPE = 1;

    /**
     * 根据用户宠物信息获取智能推荐词
     *
     * @param userId 用户ID
     * @param limit   返回数量（默认4条）
     * @return 推荐词列表
     */
    @Override
    public List<SuggestWordVO> getSuggestWords(Long userId, Integer limit)
    {
        if (limit == null || limit <= 0)
        {
            limit = DEFAULT_LIMIT;
        }

        // 1. 查询当前用户的全部宠物（PetInfo 已配置 @TableLogic，自动过滤已删除记录）
        List<PetInfo> pets = petInfoMapper.selectList(
                new LambdaQueryWrapper<PetInfo>().eq(PetInfo::getUserId, userId)
        );

        // 2. 统计宠物类型分布，例如 {cat: 2, dog: 1}
        Map<String, Integer> petTypeCounts = new HashMap<>();
        for (PetInfo pet : pets)
        {
            String type = mapBreedToType(pet.getBreed());
            if (!StringUtils.isNotBlank(type))
            {
                continue;
            }
            petTypeCounts.merge(type, 1, Integer::sum);
        }

        // 3. 无宠物 → 直接返回通用热门词
        if (petTypeCounts.isEmpty())
        {
            return getGeneralWords(limit);
        }

        // 4. 计算每种宠物类型的推荐词名额分配
        Map<String, Integer> allocation = allocateSlots(petTypeCounts, limit);

        // 5. 按分配方案查询推荐词
        List<SuggestWordVO> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : allocation.entrySet())
        {
            String type = entry.getKey();
            int count = entry.getValue();
            if (count <= 0)
            {
                continue;
            }
            List<AiSuggestWord> words = aiSuggestWordMapper.selectList(
                    new LambdaQueryWrapper<AiSuggestWord>()
                            .eq(AiSuggestWord::getPetType, type)
                            .eq(AiSuggestWord::getStatus, "1")
                            .orderByDesc(AiSuggestWord::getWeight)
                            .last("LIMIT " + count)
            );
            for (AiSuggestWord word : words)
            {
                SuggestWordVO vo = new SuggestWordVO();
                vo.setId(word.getId());
                vo.setWord(word.getWord());
                vo.setPetType(word.getPetType());
                vo.setCategory(word.getCategory());
                vo.setScore(calculateScore(word, petTypeCounts));
                result.add(vo);
            }
        }

        // 6. 按得分降序排序
        result.sort(Comparator.comparingInt(SuggestWordVO::getScore).reversed());

        // 7. 限制最终返回数量
        return result.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 品种映射到宠物类型
     * 根据 pet_info 表的 breed 字段映射到 ai_suggest_word.pet_type
     * 无法识别的品种返回空字符串，调用方会跳过该宠物（不计入类型统计）
     *
     * @param breed 宠物品种
     * @return 宠物类型（cat/dog/rabbit/bird/fish），无法识别时返回空串
     */
    private String mapBreedToType(String breed)
    {
        if (!StringUtils.isNotBlank(breed))
        {
            return "";
        }
        // 猫：英短、美短、布偶、暹罗、橘猫、狸花猫、波斯猫、缅因猫等
        if (breed.contains("猫") || breed.contains("短") || breed.contains("偶") || breed.contains("暹罗"))
        {
            return "cat";
        }
        // 狗：金毛、泰迪、柯基、柴犬、哈士奇、边牧、拉布拉多、博美等
        if (breed.contains("狗") || breed.contains("犬") || breed.contains("泰迪") || breed.contains("金毛")
                || breed.contains("柯基") || breed.contains("柴犬") || breed.contains("哈士奇")
                || breed.contains("边牧") || breed.contains("拉布拉多"))
        {
            return "dog";
        }
        if (breed.contains("兔"))
        {
            return "rabbit";
        }
        if (breed.contains("鸟") || breed.contains("鹦鹉") || breed.contains("金丝雀"))
        {
            return "bird";
        }
        if (breed.contains("鱼") || breed.contains("鲤"))
        {
            return "fish";
        }
        return "";
    }

    /**
     * 推荐词名额分配算法
     * 1. 通用类型固定分配 2 条（不超过总名额的一定比例）
     * 2. 每种宠物类型保底 1 条
     * 3. 剩余名额按宠物数量占比分配
     * 4. 因向下取整产生的零头分给宠物数量最多的类型
     *
     * @param petTypeCounts 宠物类型计数
     * @param totalSlots    总名额
     * @return 各类型分配名额
     */
    private Map<String, Integer> allocateSlots(Map<String, Integer> petTypeCounts, int totalSlots)
    {
        Map<String, Integer> allocation = new LinkedHashMap<>();
        Set<String> types = petTypeCounts.keySet();
        int typeCount = types.size();

        // 1. 通用类型固定名额（最多 GENERAL_SLOTS，且不超过 totalSlots / (typeCount+1)）
        int generalSlots = Math.min(GENERAL_SLOTS, totalSlots / (typeCount + 1));
        if (generalSlots <= 0)
        {
            // 名额极少时通用也保底1条
            generalSlots = Math.min(1, totalSlots);
        }
        allocation.put("general", generalSlots);

        // 2. 每种宠物类型保底名额
        int usedSlots = generalSlots;
        for (String type : types)
        {
            allocation.put(type, MIN_PER_TYPE);
            usedSlots += MIN_PER_TYPE;
        }

        // 3. 剩余名额按宠物数量占比分配
        int remaining = totalSlots - usedSlots;
        int totalPets = petTypeCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (remaining > 0 && totalPets > 0)
        {
            for (String type : types)
            {
                int extra = (int) Math.floor(remaining * (petTypeCounts.get(type) / (double) totalPets));
                allocation.merge(type, extra, Integer::sum);
            }
        }

        // 4. 因向下取整产生的零头分给宠物数量最多的类型
        int finalUsed = allocation.values().stream().mapToInt(Integer::intValue).sum();
        int leftover = totalSlots - finalUsed;
        if (leftover > 0)
        {
            String maxType = petTypeCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("general");
            allocation.merge(maxType, leftover, Integer::sum);
        }

        return allocation;
    }

    /**
     * 计算推荐词得分
     * 最终得分 = 基础权重 × 类型匹配系数 × 随机扰动
     * - 通用类型匹配系数 0.8
     * - 命中用户宠物类型匹配系数 1.0
     * - 随机扰动范围 0.95 ~ 1.05，避免每次返回顺序完全一致
     *
     * @param word          推荐词
     * @param petTypeCounts 用户宠物类型计数
     * @return 推荐得分
     */
    private int calculateScore(AiSuggestWord word, Map<String, Integer> petTypeCounts)
    {
        int baseWeight = word.getWeight() != null ? word.getWeight() : 50;
        double typeMatch = "general".equals(word.getPetType()) ? 0.8 : 1.0;
        double randomFactor = 0.95 + ThreadLocalRandom.current().nextDouble(0.10);
        return (int) Math.round(baseWeight * typeMatch * randomFactor);
    }

    /**
     * 获取通用热门词（无宠物用户冷启动）
     * 添加随机扰动，避免每次返回顺序完全一致
     *
     * @param limit 返回数量
     * @return 推荐词列表
     */
    private List<SuggestWordVO> getGeneralWords(int limit)
    {
        List<AiSuggestWord> words = aiSuggestWordMapper.selectList(
                new LambdaQueryWrapper<AiSuggestWord>()
                        .eq(AiSuggestWord::getPetType, "general")
                        .eq(AiSuggestWord::getStatus, "1")
                        .orderByDesc(AiSuggestWord::getWeight)
                        .last("LIMIT " + limit)
        );
        return words.stream().map(w -> {
            SuggestWordVO vo = new SuggestWordVO();
            vo.setId(w.getId());
            vo.setWord(w.getWord());
            vo.setPetType(w.getPetType());
            vo.setCategory(w.getCategory());
            // 通用词也叠加随机扰动（0.95~1.05），避免每次返回完全一致
            double randomFactor = 0.95 + ThreadLocalRandom.current().nextDouble(0.10);
            vo.setScore((int) Math.round(w.getWeight() * randomFactor));
            return vo;
        }).sorted(Comparator.comparingInt(SuggestWordVO::getScore).reversed())
          .collect(Collectors.toList());
    }

    /**
     * 分页查询推荐词列表（后台管理）
     * 支持按提示词内容、宠物类型、分类、状态筛选
     *
     * @param dto 查询参数
     * @return 推荐词列表
     */
    @Override
    public List<AiSuggestWord> getSuggestWordList(AiSuggestWordDTO dto)
    {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<AiSuggestWord> wrapper = new LambdaQueryWrapper<>();
        // 提示词内容模糊查询
        if (StringUtils.isNotBlank(dto.getWord()))
        {
            wrapper.like(AiSuggestWord::getWord, dto.getWord());
        }
        // 宠物类型精确查询
        if (StringUtils.isNotBlank(dto.getPetType()))
        {
            wrapper.eq(AiSuggestWord::getPetType, dto.getPetType());
        }
        // 分类精确查询
        if (StringUtils.isNotBlank(dto.getCategory()))
        {
            wrapper.eq(AiSuggestWord::getCategory, dto.getCategory());
        }
        // 状态精确查询
        if (StringUtils.isNotBlank(dto.getStatus()))
        {
            wrapper.eq(AiSuggestWord::getStatus, dto.getStatus());
        }
        // 按权重降序、创建时间倒序
        wrapper.orderByDesc(AiSuggestWord::getWeight)
               .orderByDesc(AiSuggestWord::getCreateTime);
        return aiSuggestWordMapper.selectList(wrapper);
    }

    /**
     * 获取推荐词详情（后台管理）
     *
     * @param id 主键ID
     * @return 推荐词信息
     */
    @Override
    public AiSuggestWord getSuggestWordById(Long id)
    {
        AiSuggestWord word = aiSuggestWordMapper.selectById(id);
        if (word == null)
        {
            throw new ServiceException("推荐词不存在");
        }
        return word;
    }

    /**
     * 新增推荐词（后台管理）
     *
     * @param dto 推荐词信息
     */
    @Override
    public void addSuggestWord(AiSuggestWordDTO dto)
    {
        AiSuggestWord word = new AiSuggestWord();
        word.setWord(dto.getWord());
        word.setPetType(dto.getPetType());
        word.setCategory(StringUtils.isBlank(dto.getCategory()) ? "general" : dto.getCategory());
        word.setWeight(dto.getWeight() == null ? 100 : dto.getWeight());
        word.setStatus(StringUtils.isBlank(dto.getStatus()) ? "1" : dto.getStatus());
        word.setCreateTime(new Date());
        word.setUpdateTime(new Date());
        aiSuggestWordMapper.insert(word);
    }

    /**
     * 编辑推荐词（后台管理）
     *
     * @param dto 推荐词信息
     */
    @Override
    public void updateSuggestWord(AiSuggestWordDTO dto)
    {
        AiSuggestWord exist = aiSuggestWordMapper.selectById(dto.getId());
        if (exist == null)
        {
            throw new ServiceException("推荐词不存在");
        }
        exist.setWord(dto.getWord());
        exist.setPetType(dto.getPetType());
        if (StringUtils.isNotBlank(dto.getCategory()))
        {
            exist.setCategory(dto.getCategory());
        }
        if (dto.getWeight() != null)
        {
            exist.setWeight(dto.getWeight());
        }
        if (StringUtils.isNotBlank(dto.getStatus()))
        {
            exist.setStatus(dto.getStatus());
        }
        exist.setUpdateTime(new Date());
        aiSuggestWordMapper.updateById(exist);
    }

    /**
     * 删除推荐词（逻辑删除，后台管理）
     *
     * @param id 主键ID
     */
    @Override
    public void deleteSuggestWord(Long id)
    {
        AiSuggestWord exist = aiSuggestWordMapper.selectById(id);
        if (exist == null)
        {
            throw new ServiceException("推荐词不存在");
        }
        // AiSuggestWord 已配置 @TableLogic，deleteById 会自动执行逻辑删除
        aiSuggestWordMapper.deleteById(id);
    }
}

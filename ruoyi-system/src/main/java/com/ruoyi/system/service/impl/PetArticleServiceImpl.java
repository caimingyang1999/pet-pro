package com.ruoyi.system.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.PetArticle;
import com.ruoyi.system.mapper.PetArticleMapper;
import com.ruoyi.system.service.IPetArticleService;

/**
 * 养宠知识文章 服务层实现
 *
 * @author ruoyi
 */
@Service
public class PetArticleServiceImpl extends ServiceImpl<PetArticleMapper, PetArticle> implements IPetArticleService
{
    /** 已发布状态 */
    private static final String STATUS_PUBLISHED = "1";

    /** 草稿状态 */
    private static final String STATUS_DRAFT = "0";

    /**
     * 允许参与兴趣优先排序的宠物类型白名单
     *
     * 取值与 ai_suggest_word.pet_type 统一：general-通用 / cat-猫 / dog-狗 /
     * rabbit-兔子 / bird-鸟 / fish-鱼 / other-其他。
     * general 表示"所有宠物都适用"，属于兜底档而不参与优先；
     * other 是"未记录"的兜底值，同样不参与优先。
     */
    private static final Set<String> INTEREST_PET_TYPES =
            new HashSet<>(Arrays.asList("cat", "dog", "rabbit", "bird", "fish"));

    /** 通用（所有宠物都适用），文章未指定适用宠物时使用 */
    private static final String PET_TYPE_GENERAL = "general";

    @Override
    public List<PetArticle> getPublishedList(String category, String keyword, String petType)
    {
        // petType 会进入 SQL 排序表达式，仅接受白名单值，非法值一律忽略
        String interestType = (petType != null && INTEREST_PET_TYPES.contains(petType)) ? petType : null;
        return baseMapper.selectPublishedList(category, keyword, interestType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PetArticle getPublishedDetail(Long id)
    {
        PetArticle article = baseMapper.selectById(id);
        // 未发布的草稿对外一律不可见
        if (article == null || !STATUS_PUBLISHED.equals(article.getStatus()))
        {
            throw new ServiceException("文章不存在或已下架");
        }
        // 累加浏览量（用 SQL 自增，避免并发覆盖）
        UpdateWrapper<PetArticle> update = new UpdateWrapper<>();
        update.eq("id", id).setSql("view_count = view_count + 1");
        baseMapper.update(null, update);
        article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
        return article;
    }

    @Override
    public List<PetArticle> getAdminList(PetArticle query)
    {
        LambdaQueryWrapper<PetArticle> wrapper = new LambdaQueryWrapper<>();
        if (query != null)
        {
            if (StringUtils.hasText(query.getTitle()))
            {
                wrapper.like(PetArticle::getTitle, query.getTitle());
            }
            if (StringUtils.hasText(query.getCategory()))
            {
                wrapper.eq(PetArticle::getCategory, query.getCategory());
            }
            if (StringUtils.hasText(query.getStatus()))
            {
                wrapper.eq(PetArticle::getStatus, query.getStatus());
            }
            if (StringUtils.hasText(query.getPetType()))
            {
                wrapper.eq(PetArticle::getPetType, query.getPetType());
            }
        }
        wrapper.orderByAsc(PetArticle::getSortOrder)
               .orderByDesc(PetArticle::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addArticle(PetArticle article)
    {
        if (!StringUtils.hasText(article.getTitle()))
        {
            throw new ServiceException("请填写文章标题");
        }
        article.setId(null);
        if (article.getViewCount() == null)
        {
            article.setViewCount(0);
        }
        if (article.getSortOrder() == null)
        {
            article.setSortOrder(0);
        }
        if (!StringUtils.hasText(article.getStatus()))
        {
            article.setStatus(STATUS_PUBLISHED);
        }
        // 未指定适用宠物时按"通用"处理，避免出现既不属于任何宠物类型、
        // 又不在兴趣推荐兜底档里的空白文章
        if (!StringUtils.hasText(article.getPetType()))
        {
            article.setPetType(PET_TYPE_GENERAL);
        }
        return baseMapper.insert(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateArticle(PetArticle article)
    {
        if (article.getId() == null)
        {
            throw new ServiceException("缺少文章ID");
        }
        // 浏览量只能由阅读行为累加，不允许后台直接覆盖
        article.setViewCount(null);
        return baseMapper.updateById(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(PetArticle article)
    {
        if (article == null || article.getId() == null)
        {
            throw new ServiceException("缺少文章ID");
        }
        String status = article.getStatus();
        if (!STATUS_DRAFT.equals(status) && !STATUS_PUBLISHED.equals(status))
        {
            throw new ServiceException("文章状态不合法");
        }
        // 只更新状态，正文等其它字段保持原值（MyBatis-Plus 默认忽略 null 字段）
        PetArticle update = new PetArticle();
        update.setId(article.getId());
        update.setStatus(status);
        update.setUpdateBy(article.getUpdateBy());
        return baseMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteArticle(Long id)
    {
        return baseMapper.deleteById(id);
    }

    @Override
    public PetArticle getArticleById(Long id)
    {
        return baseMapper.selectById(id);
    }
}

package com.ruoyi.system.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.AiSuggestWord;
import com.ruoyi.system.domain.dto.AiSuggestWordDTO;
import com.ruoyi.system.domain.vo.SuggestWordVO;

/**
 * AI搜索推荐词 服务层
 *
 * @author ruoyi
 */
public interface IAiSuggestService extends IService<AiSuggestWord>
{
    /**
     * 根据用户宠物信息获取智能推荐词（小程序端）
     *
     * @param userId 用户ID
     * @param limit   返回数量（默认8条）
     * @return 推荐词列表
     */
    List<SuggestWordVO> getSuggestWords(Long userId, Integer limit);

    /**
     * 分页查询推荐词列表（后台管理）
     *
     * @param dto 查询参数（word-提示词模糊查询，petType-宠物类型，category-分类，status-状态）
     * @return 推荐词分页列表
     */
    List<AiSuggestWord> getSuggestWordList(AiSuggestWordDTO dto);

    /**
     * 获取推荐词详情（后台管理）
     *
     * @param id 主键ID
     * @return 推荐词信息
     */
    AiSuggestWord getSuggestWordById(Long id);

    /**
     * 新增推荐词（后台管理）
     *
     * @param dto 推荐词信息
     */
    void addSuggestWord(AiSuggestWordDTO dto);

    /**
     * 编辑推荐词（后台管理）
     *
     * @param dto 推荐词信息
     */
    void updateSuggestWord(AiSuggestWordDTO dto);

    /**
     * 删除推荐词（逻辑删除，后台管理）
     *
     * @param id 主键ID
     */
    void deleteSuggestWord(Long id);
}

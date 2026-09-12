package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.PetArticle;

/**
 * 养宠知识文章 服务层
 *
 * 小程序端只读（无需登录即可浏览），后台由管理员维护。
 *
 * @author ruoyi
 */
public interface IPetArticleService
{
    /**
     * 小程序端：查询已发布文章列表
     *
     * @param category 分类（可选）
     * @param keyword  标题/摘要关键词（可选）
     * @param petType  宠物类型（cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼，可选）
     *                 传入时按"兴趣优先"排序：同类型文章最前、通用文章次之、其它最后；
     *                 不传（或非白名单值，如 general/other）时按平台默认排序
     * @return 文章集合（按排序号升序、ID降序）
     */
    public List<PetArticle> getPublishedList(String category, String keyword, String petType);

    /**
     * 小程序端：查询已发布文章详情，并累加浏览量
     *
     * @param id 文章ID
     * @return 文章详情
     */
    public PetArticle getPublishedDetail(Long id);

    /**
     * 后台：查询文章列表（含草稿）
     *
     * @param query 查询条件（title-标题模糊，category-分类，status-状态）
     * @return 文章集合
     */
    public List<PetArticle> getAdminList(PetArticle query);

    /**
     * 后台：新增文章
     *
     * @param article 文章
     * @return 影响行数
     */
    public int addArticle(PetArticle article);

    /**
     * 后台：修改文章
     *
     * @param article 文章
     * @return 影响行数
     */
    public int updateArticle(PetArticle article);

    /**
     * 后台：修改文章发布状态（草稿/已发布）
     *
     * 仅更新状态与更新人字段，避免列表页快捷切换时覆盖正文等其它内容。
     *
     * @param article 需包含 id、status（可选 updateBy）
     * @return 影响行数
     */
    public int changeStatus(PetArticle article);

    /**
     * 后台：删除文章（逻辑删除）
     *
     * @param id 文章ID
     * @return 影响行数
     */
    public int deleteArticle(Long id);

    /**
     * 后台：查询文章详情（含草稿）
     *
     * @param id 文章ID
     * @return 文章详情
     */
    public PetArticle getArticleById(Long id);
}

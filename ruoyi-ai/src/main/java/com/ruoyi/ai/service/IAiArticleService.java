package com.ruoyi.ai.service;

import com.ruoyi.ai.domain.dto.ArticleGenerateRequest;
import com.ruoyi.system.domain.PetArticle;

/**
 * 养宠知识文章 AI 生成服务
 *
 * @author ruoyi
 */
public interface IAiArticleService
{
    /**
     * 调用 Node.js 文章生成服务生成一篇养宠知识文章，
     * 将配图转存到本地上传目录（/profile）后，落库为草稿（status=0），返回文章实体。
     *
     * 落库为草稿是为了让人工审核内容（尤其是医疗健康类）后再发布，避免误导用户。
     *
     * @param request 生成参数（宠物类型 / 分类 / 选题 / 是否配图 / 配图数量）
     * @return 已落库的文章实体（含主键）
     */
    PetArticle generateAndSaveArticle(ArticleGenerateRequest request);
}

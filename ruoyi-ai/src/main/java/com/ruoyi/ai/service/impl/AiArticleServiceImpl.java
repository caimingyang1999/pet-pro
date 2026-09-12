package com.ruoyi.ai.service.impl;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.ai.config.AiArticleConfig;
import com.ruoyi.ai.domain.dto.ArticleGenerateRequest;
import com.ruoyi.ai.domain.dto.ArticleGenerateResult;
import com.ruoyi.ai.service.IAiArticleService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.system.domain.PetArticle;
import com.ruoyi.system.service.IPetArticleService;

/**
 * 养宠知识文章 AI 生成服务实现
 *
 * 对接 Node.js 文章生成服务：生成文章正文与 base64 配图，
 * 由后端把图片转存到本地上传目录（/profile），再把正文中的占位符替换为真实地址，
 * 最终以草稿形态落库，等待人工审核发布。
 *
 * @author ruoyi
 */
@Service
public class AiArticleServiceImpl implements IAiArticleService
{
    @Resource
    private WebClient aiArticleWebClient;

    @Resource
    private AiArticleConfig aiArticleConfig;

    @Resource
    private IPetArticleService petArticleService;

    @Override
    public PetArticle generateAndSaveArticle(ArticleGenerateRequest request)
    {
        // 1. 组装请求并调用 Node.js 文章生成服务
        Map<String, Object> body = new HashMap<>(8);
        body.put("petType", request.getPetType());
        body.put("category", request.getCategory());
        body.put("topic", request.getTopic());
        // 不传时 Node 默认 true，这里显式带出，避免前端遗漏
        body.put("withImage", request.getWithImage() != null ? request.getWithImage() : true);
        if (request.getInlineCount() != null)
        {
            body.put("inlineCount", request.getInlineCount());
        }

        String raw;
        try
        {
            raw = aiArticleWebClient.post()
                    .uri(aiArticleConfig.getGeneratePath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(120));
        }
        catch (Exception e)
        {
            throw new ServiceException("调用文章生成服务失败：" + e.getMessage());
        }

        if (!StringUtils.hasText(raw))
        {
            throw new ServiceException("文章生成服务返回为空");
        }

        ArticleGenerateResult result = JSON.parseObject(raw, ArticleGenerateResult.class);
        if (result == null || !Boolean.TRUE.equals(result.getSuccess()))
        {
            String msg = result != null && StringUtils.hasText(result.getMessage())
                    ? result.getMessage() : "文章生成服务返回异常";
            throw new ServiceException(msg);
        }

        ArticleGenerateResult.Data data = result.getData();
        if (data == null || !StringUtils.hasText(data.getTitle()) || !StringUtils.hasText(data.getContent()))
        {
            throw new ServiceException("文章生成结果不完整（缺少标题或正文），请重试");
        }

        // 2. 组装文章实体，先落为草稿
        PetArticle article = new PetArticle();
        article.setTitle(data.getTitle());
        article.setSummary(data.getSummary());
        article.setCategory(data.getCategory());
        article.setPetType(data.getPetType());
        article.setTags(data.getTags());
        article.setSource(data.getSource());
        // 草稿：需人工审核后发布，绝不自动发布（养宠健康内容有误会误导用户）
        article.setStatus("0");
        article.setViewCount(0);
        article.setSortOrder(0);

        // 3. 封面图转存
        if (data.getCover() != null && StringUtils.hasText(data.getCover().getBase64()))
        {
            article.setCoverImage(storeImage(data.getCover().getBase64(), data.getCover().getExt()));
        }

        // 4. 正文配图转存，并把占位符替换成真实地址
        String content = data.getContent();
        List<ArticleGenerateResult.InlineImage> images = data.getImages();
        if (images != null)
        {
            for (ArticleGenerateResult.InlineImage img : images)
            {
                if (img == null || !StringUtils.hasText(img.getToken())
                        || !StringUtils.hasText(img.getBase64()))
                {
                    continue;
                }
                String url = storeImage(img.getBase64(), img.getExt());
                // 占位符出现在 <img src="__ARTICLE_IMG_n__"> 中，整串替换即可
                content = content.replace(img.getToken(), url);
            }
        }
        article.setContent(content);

        // 5. 记录创建人并落库
        article.setCreateBy(SecurityUtils.getUsername());
        petArticleService.addArticle(article);
        return article;
    }

    /**
     * 将 base64 图片转存到本地上传目录，返回 /profile/... 访问路径
     */
    private String storeImage(String base64, String ext)
    {
        try
        {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return FileUtils.writeBytes(bytes, RuoYiConfig.getUploadPath());
        }
        catch (Exception e)
        {
            throw new ServiceException("文章配图转存失败：" + e.getMessage());
        }
    }
}

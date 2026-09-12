package com.ruoyi.ai.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.config.AiArticleConfig;
import com.ruoyi.ai.domain.dto.ArticleGenerateRequest;
import com.ruoyi.ai.service.IAiArticleService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.PetArticle;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 养宠知识文章 AI 生成 控制器
 *
 * 与后台文章管理共用 /api/v1/admin/articles 前缀，便于复用权限与前端菜单。
 * 生成结果落库为草稿，由管理员审核后再发布。
 *
 * @author ruoyi
 */
@Api(tags = "养宠知识-AI生成")
@RestController
@RequestMapping("/api/v1/admin/articles")
public class AiArticleController extends BaseController
{
    @Resource
    private IAiArticleService aiArticleService;

    @Resource
    private WebClient aiArticleWebClient;

    @Resource
    private AiArticleConfig aiArticleConfig;

    /**
     * AI 生成一篇养宠知识文章（落库为草稿）
     *
     * @param request 生成参数（petType / category / topic / withImage / inlineCount）
     * @return 已生成的草稿文章
     */
    @ApiOperation("AI 生成文章（落库为草稿）")
    @PreAuthorize("@ss.hasPermi('article:add')")
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody ArticleGenerateRequest request)
    {
        PetArticle article = aiArticleService.generateAndSaveArticle(request);
        return AjaxResult.success("已生成草稿，请审核内容后发布", article);
    }

    /**
     * AI 文章生成能力探测：供前端判断是否启用 AI 配图、展示模型信息
     *
     * 探测失败不阻断界面，仅返回 available=false
     */
    @ApiOperation("AI 文章生成能力探测")
    @PreAuthorize("@ss.hasPermi('article:add')")
    @GetMapping("/generate/status")
    public AjaxResult status()
    {
        Map<String, Object> resp = new HashMap<>(4);
        try
        {
            String raw = aiArticleWebClient.get()
                    .uri(aiArticleConfig.getStatusPath())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(10));
            JSONObject obj = JSON.parseObject(raw);
            JSONObject data = obj != null ? obj.getJSONObject("data") : null;
            resp.put("available", true);
            resp.put("aiImageEnabled", data != null && data.getBooleanValue("aiImageEnabled"));
            resp.put("imageModel", data != null ? data.getString("imageModel") : null);
            resp.put("articleModel", data != null ? data.getString("articleModel") : null);
        }
        catch (Exception e)
        {
            resp.put("available", false);
            resp.put("aiImageEnabled", false);
        }
        return AjaxResult.success(resp);
    }
}

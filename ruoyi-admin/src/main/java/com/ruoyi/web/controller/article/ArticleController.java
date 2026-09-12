package com.ruoyi.web.controller.article;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.PetArticle;
import com.ruoyi.system.service.IPetArticleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 小程序端-养宠知识 控制器
 *
 * 内容由平台发布，用户只读浏览；不提供评论、点赞、分享等互动能力。
 * 允许匿名访问：游客同样可以浏览养宠知识。
 *
 * @author ruoyi
 */
@Api(tags = "小程序端-养宠知识")
@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController extends BaseController
{
    @Resource
    private IPetArticleService petArticleService;

    /**
     * 养宠知识文章列表（仅返回已发布文章）
     *
     * @param category 分类（可选）
     * @param keyword  关键词（可选，匹配标题/摘要）
     * @param petType  宠物类型（可选，cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼）
     *                 传入后按"兴趣优先"排序：与用户宠物类型一致的文章最前，
     *                 通用文章次之，其它最后；不传则按平台默认排序
     * @return 文章分页列表
     */
    @ApiOperation("养宠知识列表")
    @Anonymous
    @GetMapping("/list")
    public TableDataInfo list(
            @ApiParam(name = "category", value = "分类") @RequestParam(required = false) String category,
            @ApiParam(name = "keyword", value = "搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam(name = "petType", value = "宠物类型（cat/dog/rabbit/bird/fish）") @RequestParam(required = false) String petType)
    {
        startPage();
        List<PetArticle> list = petArticleService.getPublishedList(category, keyword, petType);
        return getDataTable(list);
    }

    /**
     * 养宠知识文章详情（浏览量 +1）
     *
     * @param id 文章ID
     * @return 文章详情
     */
    @ApiOperation("养宠知识详情")
    @Anonymous
    @GetMapping("/{id}")
    public AjaxResult getInfo(
            @ApiParam(name = "id", value = "文章ID", required = true)
            @PathVariable Long id)
    {
        return AjaxResult.success(petArticleService.getPublishedDetail(id));
    }
}

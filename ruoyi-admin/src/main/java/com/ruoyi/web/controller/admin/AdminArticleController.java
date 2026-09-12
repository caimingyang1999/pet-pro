package com.ruoyi.web.controller.admin;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.PetArticle;
import com.ruoyi.system.service.IPetArticleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台-养宠知识文章管理 控制器
 *
 * @author ruoyi
 */
@Api(tags = "后台-养宠知识管理")
@RestController
@RequestMapping("/api/v1/admin/articles")
@PreAuthorize("@ss.hasPermi('article:list')")
public class AdminArticleController extends BaseController
{
    @Resource
    private IPetArticleService petArticleService;

    /**
     * 文章列表（含草稿）
     *
     * @param query 查询条件（title-标题模糊，category-分类，status-状态）
     */
    @ApiOperation("文章列表")
    @GetMapping("/list")
    public TableDataInfo list(PetArticle query)
    {
        startPage();
        List<PetArticle> list = petArticleService.getAdminList(query);
        return getDataTable(list);
    }

    /**
     * 文章详情
     *
     * @param id 文章ID
     */
    @ApiOperation("文章详情")
    @GetMapping("/{id}")
    public AjaxResult getInfo(
            @ApiParam(name = "id", value = "文章ID", required = true)
            @PathVariable Long id)
    {
        return AjaxResult.success(petArticleService.getArticleById(id));
    }

    /**
     * 新增文章
     *
     * @param article 文章
     */
    @ApiOperation("新增文章")
    @PreAuthorize("@ss.hasPermi('article:add')")
    @PostMapping
    public AjaxResult add(@RequestBody PetArticle article)
    {
        article.setCreateBy(SecurityUtils.getUsername());
        return toAjax(petArticleService.addArticle(article));
    }

    /**
     * 修改文章
     *
     * @param id      文章ID
     * @param article 文章
     */
    @ApiOperation("修改文章")
    @PreAuthorize("@ss.hasPermi('article:edit')")
    @PutMapping("/{id}")
    public AjaxResult edit(
            @ApiParam(name = "id", value = "文章ID", required = true)
            @PathVariable Long id,
            @RequestBody PetArticle article)
    {
        article.setId(id);
        article.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(petArticleService.updateArticle(article));
    }

    /**
     * 修改文章发布状态（列表页快捷切换草稿/已发布）
     *
     * @param id     文章ID
     * @param status 状态（0-草稿 1-已发布）
     */
    @ApiOperation("修改文章状态")
    @PreAuthorize("@ss.hasPermi('article:edit')")
    @PutMapping("/{id}/status")
    public AjaxResult changeStatus(
            @ApiParam(name = "id", value = "文章ID", required = true)
            @PathVariable Long id,
            @ApiParam(name = "status", value = "状态（0-草稿 1-已发布）", required = true)
            @RequestParam String status)
    {
        PetArticle article = new PetArticle();
        article.setId(id);
        article.setStatus(status);
        article.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(petArticleService.changeStatus(article));
    }

    /**
     * 删除文章
     *
     * @param id 文章ID
     */
    @ApiOperation("删除文章")
    @PreAuthorize("@ss.hasPermi('article:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(
            @ApiParam(name = "id", value = "文章ID", required = true)
            @PathVariable Long id)
    {
        return toAjax(petArticleService.deleteArticle(id));
    }
}

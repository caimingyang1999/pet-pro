package com.ruoyi.web.controller.admin;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.AiSuggestWord;
import com.ruoyi.system.domain.dto.AiSuggestWordDTO;
import com.ruoyi.system.service.IAiSuggestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台-AI搜索推荐词管理 控制器
 * 提供推荐词的分页查询、详情、新增、编辑、删除功能。
 *
 * @author ruoyi
 */
@Api(tags = "后台-AI搜索推荐词管理")
@RestController
@RequestMapping("/api/v1/admin/suggest-words")
@PreAuthorize("@ss.hasPermi('admin')")
public class AdminAiSuggestController extends BaseController
{
    @Resource
    private IAiSuggestService aiSuggestService;

    /**
     * 分页查询推荐词列表
     *
     * @param dto 查询参数（word-提示词模糊查询，petType-宠物类型，category-分类，status-状态）
     * @return 推荐词分页列表
     */
    @ApiOperation("分页查询推荐词列表")
    @Log(title = "AI搜索推荐词管理", businessType = BusinessType.OTHER)
    @GetMapping("/list")
    public TableDataInfo list(AiSuggestWordDTO dto)
    {
        startPage();
        List<AiSuggestWord> list = aiSuggestService.getSuggestWordList(dto);
        return getDataTable(list);
    }

    /**
     * 获取推荐词详情
     *
     * @param id 推荐词ID
     * @return 推荐词信息
     */
    @ApiOperation("获取推荐词详情")
    @Log(title = "AI搜索推荐词管理", businessType = BusinessType.OTHER)
    @GetMapping("/{id}")
    public AjaxResult getInfo(
            @ApiParam(name = "id", value = "推荐词ID", required = true)
            @PathVariable Long id)
    {
        return AjaxResult.success(aiSuggestService.getSuggestWordById(id));
    }

    /**
     * 新增推荐词
     *
     * @param dto 推荐词信息
     * @return 操作结果
     */
    @ApiOperation("新增推荐词")
    @Log(title = "AI搜索推荐词管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AiSuggestWordDTO dto)
    {
        aiSuggestService.addSuggestWord(dto);
        return AjaxResult.success("新增成功");
    }

    /**
     * 编辑推荐词
     *
     * @param id  推荐词ID
     * @param dto 推荐词信息
     * @return 操作结果
     */
    @ApiOperation("编辑推荐词")
    @Log(title = "AI搜索推荐词管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(
            @ApiParam(name = "id", value = "推荐词ID", required = true)
            @PathVariable Long id,
            @Validated @RequestBody AiSuggestWordDTO dto)
    {
        dto.setId(id);
        aiSuggestService.updateSuggestWord(dto);
        return AjaxResult.success("编辑成功");
    }

    /**
     * 删除推荐词（逻辑删除）
     *
     * @param id 推荐词ID
     * @return 操作结果
     */
    @ApiOperation("删除推荐词")
    @Log(title = "AI搜索推荐词管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(
            @ApiParam(name = "id", value = "推荐词ID", required = true)
            @PathVariable Long id)
    {
        aiSuggestService.deleteSuggestWord(id);
        return AjaxResult.success("删除成功");
    }
}

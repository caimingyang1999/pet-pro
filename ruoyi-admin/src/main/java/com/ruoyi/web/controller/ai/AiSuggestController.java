package com.ruoyi.web.controller.ai;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.vo.SuggestWordVO;
import com.ruoyi.system.service.IAiSuggestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * AI搜索推荐词 控制器
 * 根据当前用户已添加的宠物信息，智能推荐AI对话页搜索提示词。
 *
 * @author ruoyi
 */
@Api(tags = "AI搜索推荐词")
@RestController
@RequestMapping("/api/v1/ai")
public class AiSuggestController extends BaseController
{
    @Resource
    private IAiSuggestService aiSuggestService;

    /**
     * 获取AI搜索推荐词
     * 根据当前用户宠物类型分布智能推荐，无宠物时返回通用热门词。
     *
     * @param limit 返回数量，默认4条
     * @return 推荐词列表
     */
    @ApiOperation("获取AI搜索推荐词")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/suggest-words")
    public AjaxResult getSuggestWords(
            @ApiParam(name = "limit", value = "返回数量，默认4条")
            @RequestParam(defaultValue = "4") Integer limit)
    {
        Long userId = getUserId();
        List<SuggestWordVO> words = aiSuggestService.getSuggestWords(userId, limit);
        return AjaxResult.success("获取成功", words);
    }
}

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
import com.ruoyi.system.service.ISysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 养宠顾问推荐词 控制器
 * 根据当前用户已添加的宠物信息，推荐顾问页提问提示词。
 *
 * @author ruoyi
 */
@Api(tags = "养宠顾问推荐词")
@RestController
@RequestMapping("/api/v1/adviser")
public class AiSuggestController extends BaseController
{
    /** 养宠顾问功能开关参数键（sys_config，true开启/false关闭） */
    private static final String CONFIG_KEY_ADVISER_ENABLED = "feature.adviser.enabled";

    @Resource
    private IAiSuggestService aiSuggestService;

    @Resource
    private ISysConfigService configService;

    /**
     * 获取养宠顾问推荐提问词
     * 根据当前用户宠物类型分布匹配推荐，无宠物时返回通用热门词。
     *
     * @param limit 返回数量，默认4条
     * @return 推荐词列表
     */
    @ApiOperation("获取养宠顾问推荐提问词")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/suggest-words")
    public AjaxResult getSuggestWords(
            @ApiParam(name = "limit", value = "返回数量，默认4条")
            @RequestParam(defaultValue = "4") Integer limit)
    {
        // 功能关闭时直接返回空列表，不触发推荐逻辑
        if ("false".equalsIgnoreCase(configService.selectConfigByKey(CONFIG_KEY_ADVISER_ENABLED)))
        {
            return AjaxResult.success("获取成功", new java.util.ArrayList<SuggestWordVO>());
        }
        Long userId = getUserId();
        List<SuggestWordVO> words = aiSuggestService.getSuggestWords(userId, limit);
        return AjaxResult.success("获取成功", words);
    }
}

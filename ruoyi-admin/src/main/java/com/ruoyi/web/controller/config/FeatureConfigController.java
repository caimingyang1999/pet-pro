package com.ruoyi.web.controller.config;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.ISysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 小程序功能开关配置
 * 开关存储在 sys_config 参数表，后台「参数设置」维护，Redis 缓存、修改后实时生效。
 *
 * @author ruoyi
 */
@Api(tags = "小程序功能开关")
@RestController
@RequestMapping("/api/v1/config")
public class FeatureConfigController extends BaseController
{
    /** 养宠顾问功能开关参数键（true开启/false关闭，未配置时默认开启） */
    public static final String CONFIG_KEY_ADVISER_ENABLED = "feature.adviser.enabled";

    @Resource
    private ISysConfigService configService;

    /**
     * 获取小程序功能开关（匿名访问，小程序启动时拉取）
     *
     * @return 功能开关集合，如 { adviserEnabled: true }
     */
    @ApiOperation("获取功能开关")
    @Anonymous
    @GetMapping("/features")
    public AjaxResult features()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("adviserEnabled", isAdviserEnabled());
        return AjaxResult.success(data);
    }

    /**
     * 养宠顾问开关：参数未配置时默认开启
     */
    private boolean isAdviserEnabled()
    {
        return !"false".equalsIgnoreCase(configService.selectConfigByKey(CONFIG_KEY_ADVISER_ENABLED));
    }
}

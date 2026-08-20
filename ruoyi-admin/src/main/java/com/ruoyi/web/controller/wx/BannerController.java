package com.ruoyi.web.controller.wx;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.Banner;
import com.ruoyi.system.service.IBannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 小程序端-轮播图 控制器
 *
 * @author ruoyi
 */
@Api(tags = "小程序端-轮播图")
@RestController
@RequestMapping("/api/v1/banner")
public class BannerController extends BaseController
{
    @Resource
    private IBannerService bannerService;

    /**
     * 获取启用的轮播图列表（小程序端）
     * 无需登录即可访问
     *
     * @return 轮播图列表
     */
    @ApiOperation("获取启用的轮播图列表")
    @GetMapping("/list")
    public AjaxResult getActiveBanners()
    {
        List<Banner> list = bannerService.getActiveBanners();
        return AjaxResult.success(list);
    }
}

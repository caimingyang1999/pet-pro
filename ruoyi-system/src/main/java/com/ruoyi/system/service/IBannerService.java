package com.ruoyi.system.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.Banner;
import com.ruoyi.system.domain.dto.BannerDTO;

/**
 * 轮播图 服务层
 *
 * @author ruoyi
 */
public interface IBannerService extends IService<Banner>
{
    /**
     * 获取启用的轮播图列表（小程序端调用）
     * 按sort_order排序，过滤过期时间
     *
     * @return 轮播图集合
     */
    List<Banner> getActiveBanners();

    /**
     * 分页查询轮播图列表（后台管理）
     *
     * @param dto 查询参数
     * @return 轮播图集合
     */
    List<Banner> getBannerList(BannerDTO dto);

    /**
     * 获取轮播图详情
     *
     * @param id 轮播图ID
     * @return 轮播图信息
     */
    Banner getBannerById(Long id);

    /**
     * 新增轮播图
     *
     * @param dto 轮播图信息
     */
    void addBanner(BannerDTO dto);

    /**
     * 编辑轮播图
     *
     * @param dto 轮播图信息
     */
    void updateBanner(BannerDTO dto);

    /**
     * 删除轮播图（逻辑删除）
     *
     * @param id 轮播图ID
     */
    void deleteBanner(Long id);
}

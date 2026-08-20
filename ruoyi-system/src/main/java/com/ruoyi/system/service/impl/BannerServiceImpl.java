package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.Banner;
import com.ruoyi.system.domain.dto.BannerDTO;
import com.ruoyi.system.mapper.BannerMapper;
import com.ruoyi.system.service.IBannerService;

/**
 * 轮播图 服务层实现
 *
 * @author ruoyi
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements IBannerService
{
    @Resource
    private BannerMapper bannerMapper;

    /**
     * 获取启用的轮播图列表（小程序端调用）
     * 过滤条件：
     * - status = '1'
     * - del_flag = '0'
     * - (start_time IS NULL OR start_time <= NOW())
     * - (end_time IS NULL OR end_time >= NOW())
     * 排序：sort_order ASC, id DESC
     */
    @Override
    public List<Banner> getActiveBanners()
    {
        Date now = new Date();
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, "1")
               .and(w -> w.isNull(Banner::getStartTime).or().le(Banner::getStartTime, now))
               .and(w -> w.isNull(Banner::getEndTime).or().ge(Banner::getEndTime, now))
               .orderByAsc(Banner::getSortOrder)
               .orderByDesc(Banner::getId);
        return bannerMapper.selectList(wrapper);
    }

    /**
     * 分页查询轮播图列表（后台管理）
     */
    @Override
    public List<Banner> getBannerList(BannerDTO dto)
    {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        // 标题模糊查询
        if (StringUtils.isNotBlank(dto.getTitle()))
        {
            wrapper.like(Banner::getTitle, dto.getTitle());
        }
        // 状态查询
        if (StringUtils.isNotBlank(dto.getStatus()))
        {
            wrapper.eq(Banner::getStatus, dto.getStatus());
        }
        wrapper.orderByAsc(Banner::getSortOrder)
               .orderByDesc(Banner::getId);
        return bannerMapper.selectList(wrapper);
    }

    /**
     * 获取轮播图详情
     */
    @Override
    public Banner getBannerById(Long id)
    {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null)
        {
            throw new ServiceException("轮播图不存在");
        }
        return banner;
    }

    /**
     * 新增轮播图
     */
    @Override
    public void addBanner(BannerDTO dto)
    {
        Banner banner = new Banner();
        banner.setTitle(dto.getTitle());
        banner.setImageUrl(dto.getImageUrl());
        banner.setJumpType(dto.getJumpType());
        banner.setJumpTarget(dto.getJumpTarget());
        banner.setStartTime(dto.getStartTime());
        banner.setEndTime(dto.getEndTime());
        banner.setRemark(dto.getRemark());

        // sort_order为空时默认取当前最大值+1
        if (dto.getSortOrder() == null)
        {
            Integer maxSort = getMaxSortOrder();
            banner.setSortOrder(maxSort + 1);
        }
        else
        {
            banner.setSortOrder(dto.getSortOrder());
        }

        // status为空时默认'1'
        banner.setStatus(StringUtils.isBlank(dto.getStatus()) ? "1" : dto.getStatus());

        // create_by从SecurityUtils获取当前用户
        banner.setCreateBy(SecurityUtils.getUsername());
        banner.setCreateTime(new Date());

        bannerMapper.insert(banner);
    }

    /**
     * 编辑轮播图
     */
    @Override
    public void updateBanner(BannerDTO dto)
    {
        Banner exist = bannerMapper.selectById(dto.getId());
        if (exist == null)
        {
            throw new ServiceException("轮播图不存在");
        }

        exist.setTitle(dto.getTitle());
        exist.setImageUrl(dto.getImageUrl());
        exist.setJumpType(dto.getJumpType());
        exist.setJumpTarget(dto.getJumpTarget());
        exist.setStartTime(dto.getStartTime());
        exist.setEndTime(dto.getEndTime());
        exist.setRemark(dto.getRemark());

        if (dto.getSortOrder() != null)
        {
            exist.setSortOrder(dto.getSortOrder());
        }
        if (StringUtils.isNotBlank(dto.getStatus()))
        {
            exist.setStatus(dto.getStatus());
        }

        exist.setUpdateBy(SecurityUtils.getUsername());
        exist.setUpdateTime(new Date());

        bannerMapper.updateById(exist);
    }

    /**
     * 删除轮播图（逻辑删除）
     */
    @Override
    public void deleteBanner(Long id)
    {
        Banner exist = bannerMapper.selectById(id);
        if (exist == null)
        {
            throw new ServiceException("轮播图不存在");
        }
        bannerMapper.deleteById(id);
    }

    /**
     * 获取当前最大排序号
     */
    private Integer getMaxSortOrder()
    {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Banner::getSortOrder)
               .last("LIMIT 1");
        Banner maxBanner = bannerMapper.selectOne(wrapper);
        return maxBanner != null && maxBanner.getSortOrder() != null ? maxBanner.getSortOrder() : 0;
    }
}

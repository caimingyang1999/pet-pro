package com.ruoyi.system.service.impl;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.UserPointsLog;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.UserPointsLogMapper;
import com.ruoyi.system.service.IUserPointsService;

/**
 * 积分记录 服务层实现
 *
 * @author ruoyi
 */
@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsLogMapper, UserPointsLog> implements IUserPointsService
{
    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 积分明细
     *
     * @param userId 用户ID
     * @return 积分记录集合
     */
    @Override
    public List<UserPointsLog> getPointsLog(Long userId)
    {
        LambdaQueryWrapper<UserPointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPointsLog::getUserId, userId)
               .orderByDesc(UserPointsLog::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 积分变动记录
     *
     * @param userId   用户ID
     * @param points   变动积分（正数增加，负数扣减）
     * @param type     变动类型（sign_in-签到 post-发布动态 exchange-兑换商品 admin-管理员操作）
     * @param relateId 关联业务ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Long userId, Integer points, String type, Long relateId)
    {
        // 1. 更新用户表积分余额
        int rows = sysUserMapper.updateUserPoints(userId, points);
        if (rows <= 0)
        {
            throw new ServiceException("用户不存在或积分更新失败");
        }

        // 2. 查询更新后的积分余额
        com.ruoyi.common.core.domain.entity.SysUser user = sysUserMapper.selectUserById(userId);
        if (user == null)
        {
            throw new ServiceException("用户不存在");
        }

        // 3. 如果是扣减积分，校验积分是否充足
        if (points < 0 && (user.getPoints() == null || user.getPoints() < 0))
        {
            throw new ServiceException("用户积分不足");
        }

        // 4. 记录积分变动日志
        UserPointsLog log = new UserPointsLog();
        log.setUserId(userId);
        log.setPointsChange(points);
        log.setPointsBalance(user.getPoints());
        log.setChangeType(type);
        log.setRelateId(relateId);
        baseMapper.insert(log);

        return true;
    }
}

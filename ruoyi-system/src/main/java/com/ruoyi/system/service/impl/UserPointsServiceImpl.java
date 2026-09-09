package com.ruoyi.system.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.UserPointsLog;
import com.ruoyi.system.domain.UserSignIn;
import com.ruoyi.system.domain.vo.SignInVO;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.UserPointsLogMapper;
import com.ruoyi.system.mapper.UserSignInMapper;
import com.ruoyi.system.service.IUserPointsService;

/**
 * 积分记录 服务层实现
 *
 * @author ruoyi
 */
@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsLogMapper, UserPointsLog> implements IUserPointsService
{
    /** 签到奖励积分 */
    private static final int SIGN_IN_REWARD = 5;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private UserSignInMapper userSignInMapper;

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
     * @param type     变动类型
     * @param relateId 关联业务ID
     * @return 结果
     */
    @Override
    public boolean addPoints(Long userId, Integer points, String type, Long relateId)
    {
        return addPoints(userId, points, type, relateId, null);
    }

    /**
     * 积分变动记录（带备注）
     *
     * @param userId   用户ID
     * @param points   变动积分（正数增加，负数扣减）
     * @param type     变动类型
     * @param relateId 关联业务ID
     * @param remark   备注信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Long userId, Integer points, String type, Long relateId, String remark)
    {
        // 1. 查询用户信息
        com.ruoyi.common.core.domain.entity.SysUser user = sysUserMapper.selectUserById(userId);
        if (user == null)
        {
            throw new ServiceException("用户不存在");
        }

        // 2. 如果是扣减积分，先校验积分是否充足
        if (points < 0)
        {
            int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
            if (currentPoints + points < 0)
            {
                throw new ServiceException("用户积分不足");
            }
        }

        // 3. 更新用户表积分余额（原子操作）
        int rows = sysUserMapper.updateUserPoints(userId, points);
        if (rows <= 0)
        {
            throw new ServiceException("积分更新失败");
        }

        // 4. 查询更新后的积分余额
        com.ruoyi.common.core.domain.entity.SysUser updatedUser = sysUserMapper.selectUserById(userId);

        // 5. 记录积分变动日志
        UserPointsLog log = new UserPointsLog();
        log.setUserId(userId);
        log.setPointsChange(points);
        log.setPointsBalance(updatedUser.getPoints());
        log.setChangeType(type);
        log.setRelateId(relateId);
        log.setRemark(remark);
        baseMapper.insert(log);

        return true;
    }

    /**
     * 检查用户是否已领取过指定类型的积分奖励
     *
     * @param userId 用户ID
     * @param type   变动类型
     * @return true-已领取 false-未领取
     */
    @Override
    public boolean hasReceivedReward(Long userId, String type)
    {
        LambdaQueryWrapper<UserPointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPointsLog::getUserId, userId)
               .eq(UserPointsLog::getChangeType, type);
        return baseMapper.selectCount(wrapper) > 0;
    }

    /**
     * 每日签到（每天仅一次，奖励 +5 积分）
     *
     * @param userId 用户ID
     * @return 签到结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignInVO signIn(Long userId)
    {
        // 取今日零点 Date，与数据库 DATE 列精确匹配
        Date today = getTodayDate();
        // 1. 查询今日是否已签到
        LambdaQueryWrapper<UserSignIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSignIn::getUserId, userId)
               .eq(UserSignIn::getSignDate, today);
        UserSignIn exist = userSignInMapper.selectOne(wrapper);

        SignInVO vo = new SignInVO();
        if (exist != null)
        {
            // 今日已签到
            vo.setSigned(true);
            vo.setPointsReward(0);
            vo.setMessage("今日已签到");
            com.ruoyi.common.core.domain.entity.SysUser user = sysUserMapper.selectUserById(userId);
            vo.setPointsBalance(user != null && user.getPoints() != null ? user.getPoints() : 0);
            return vo;
        }

        // 2. 插入签到记录
        UserSignIn signIn = new UserSignIn();
        signIn.setUserId(userId);
        signIn.setSignDate(today);
        signIn.setPointsReward(SIGN_IN_REWARD);
        userSignInMapper.insert(signIn);

        // 3. 发放签到积分
        addPoints(userId, SIGN_IN_REWARD, "sign_in", signIn.getId());

        // 4. 构造返回结果
        com.ruoyi.common.core.domain.entity.SysUser user = sysUserMapper.selectUserById(userId);
        vo.setSigned(true);
        vo.setPointsReward(SIGN_IN_REWARD);
        vo.setPointsBalance(user != null && user.getPoints() != null ? user.getPoints() : 0);
        vo.setMessage("签到成功，获得 " + SIGN_IN_REWARD + " 积分");
        return vo;
    }

    /**
     * 获取今日签到状态
     *
     * @param userId 用户ID
     * @return 签到结果
     */
    @Override
    public SignInVO getSignInStatus(Long userId)
    {
        Date today = getTodayDate();
        LambdaQueryWrapper<UserSignIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSignIn::getUserId, userId)
               .eq(UserSignIn::getSignDate, today);
        UserSignIn exist = userSignInMapper.selectOne(wrapper);

        SignInVO vo = new SignInVO();
        if (exist != null)
        {
            vo.setSigned(true);
            vo.setMessage("今日已签到");
        }
        else
        {
            vo.setSigned(false);
            vo.setMessage("今日未签到");
        }
        com.ruoyi.common.core.domain.entity.SysUser user = sysUserMapper.selectUserById(userId);
        vo.setPointsBalance(user != null && user.getPoints() != null ? user.getPoints() : 0);
        return vo;
    }

    /**
     * 获取今日零点 Date（清除时分秒），用于与数据库 DATE 类型列精确比较
     */
    private Date getTodayDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}

package com.ruoyi.system.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.UserPointsLog;
import com.ruoyi.system.domain.vo.SignInVO;

/**
 * 积分记录 服务层
 *
 * @author ruoyi
 */
public interface IUserPointsService extends IService<UserPointsLog>
{
    /**
     * 积分明细
     *
     * @param userId 用户ID
     * @return 积分记录集合
     */
    List<UserPointsLog> getPointsLog(Long userId);

    /**
     * 积分变动记录
     *
     * @param userId   用户ID
     * @param points   变动积分（正数增加，负数扣减）
     * @param type     变动类型（sign_in-签到 post-发布动态 exchange-兑换商品 admin-管理员操作 register-注册奖励 pet-完善宠物信息）
     * @param relateId 关联业务ID
     * @return 结果
     */
    boolean addPoints(Long userId, Integer points, String type, Long relateId);

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
    boolean addPoints(Long userId, Integer points, String type, Long relateId, String remark);

    /**
     * 检查用户是否已领取过指定类型的积分奖励（用于一次性奖励防重复发放）
     *
     * @param userId 用户ID
     * @param type   变动类型
     * @return true-已领取 false-未领取
     */
    boolean hasReceivedReward(Long userId, String type);

    /**
     * 每日签到（每天仅一次，奖励 +5 积分）
     *
     * @param userId 用户ID
     * @return 签到结果
     */
    SignInVO signIn(Long userId);

    /**
     * 获取今日签到状态
     *
     * @param userId 用户ID
     * @return 签到结果（signed-是否已签到）
     */
    SignInVO getSignInStatus(Long userId);
}

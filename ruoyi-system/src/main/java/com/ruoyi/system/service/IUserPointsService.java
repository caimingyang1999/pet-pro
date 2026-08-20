package com.ruoyi.system.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.UserPointsLog;

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
     * @param type     变动类型（sign_in-签到 post-发布动态 exchange-兑换商品 admin-管理员操作）
     * @param relateId 关联业务ID
     * @return 结果
     */
    boolean addPoints(Long userId, Integer points, String type, Long relateId);
}

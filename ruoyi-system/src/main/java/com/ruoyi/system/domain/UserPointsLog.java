package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 积分记录表 user_points_log
 *
 * @author ruoyi
 */
@Data
@TableName("user_points_log")
public class UserPointsLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 积分变动（正数为增加，负数为扣减） */
    @ApiModelProperty("积分变动（正数为增加，负数为扣减）")
    private Integer pointsChange;

    /** 变动后余额 */
    @ApiModelProperty("变动后余额")
    private Integer pointsBalance;

    /** 变动类型（sign_in-签到 post-发布动态 exchange-兑换商品 admin-管理员操作 register-注册奖励 pet-完善宠物信息） */
    @ApiModelProperty("变动类型（sign_in-签到 post-发布动态 exchange-兑换商品 admin-管理员操作 register-注册奖励 pet-完善宠物信息）")
    private String changeType;

    /** 关联业务ID */
    @ApiModelProperty("关联业务ID")
    private Long relateId;

    /** 备注 */
    @ApiModelProperty("备注")
    private String remark;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}

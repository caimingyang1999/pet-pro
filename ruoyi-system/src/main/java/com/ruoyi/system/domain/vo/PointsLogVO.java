package com.ruoyi.system.domain.vo;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 积分记录返回对象
 *
 * @author ruoyi
 */
@Data
@ApiModel("积分记录返回对象")
public class PointsLogVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("记录ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("积分变动（正数为增加，负数为扣减）")
    private Integer pointsChange;

    @ApiModelProperty("变动后余额")
    private Integer pointsBalance;

    @ApiModelProperty("变动类型（sign_in-签到 post-发布动态 exchange-兑换商品 admin-管理员操作 register-注册奖励 pet-完善宠物信息）")
    private String changeType;

    @ApiModelProperty("关联业务ID")
    private Long relateId;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}

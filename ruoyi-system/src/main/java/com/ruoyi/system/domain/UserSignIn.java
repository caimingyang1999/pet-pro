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
 * 签到记录表 user_sign_in
 *
 * @author ruoyi
 */
@Data
@TableName("user_sign_in")
public class UserSignIn implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 签到日期 */
    @ApiModelProperty("签到日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date signDate;

    /** 签到奖励积分 */
    @ApiModelProperty("签到奖励积分")
    private Integer pointsReward;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}

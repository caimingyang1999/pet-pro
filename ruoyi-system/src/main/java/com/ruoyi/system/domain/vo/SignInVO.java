package com.ruoyi.system.domain.vo;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 签到结果返回对象
 *
 * @author ruoyi
 */
@Data
@ApiModel("签到结果返回对象")
public class SignInVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("今日是否已签到")
    private Boolean signed;

    @ApiModelProperty("签到奖励积分")
    private Integer pointsReward;

    @ApiModelProperty("当前积分余额")
    private Integer pointsBalance;

    @ApiModelProperty("提示信息")
    private String message;
}

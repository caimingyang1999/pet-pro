package com.ruoyi.system.domain.vo;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户注册返回对象
 *
 * @author ruoyi
 */
@Data
@ApiModel(description = "用户注册返回对象")
public class RegisterVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "JWT Token")
    private String token;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "手机号（脱敏）")
    private String phone;

    @ApiModelProperty(value = "初始积分")
    private Integer points;
}

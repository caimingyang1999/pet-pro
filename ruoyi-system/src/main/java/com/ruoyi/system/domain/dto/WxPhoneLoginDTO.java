package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;

/**
 * 微信手机号登录请求参数
 *
 * @author ruoyi
 */
@ApiModel(description = "微信手机号登录请求参数")
public class WxPhoneLoginDTO
{
    @ApiModelProperty(value = "微信登录凭证code（uni.login返回）", required = true)
    @NotBlank(message = "code不能为空")
    private String code;

    @ApiModelProperty(value = "手机号获取凭证code（getPhoneNumber返回）", required = true)
    @NotBlank(message = "phoneCode不能为空")
    private String phoneCode;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getPhoneCode()
    {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode)
    {
        this.phoneCode = phoneCode;
    }
}

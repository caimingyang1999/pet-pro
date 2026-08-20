package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;

/**
 * 微信手机号请求参数
 *
 * @author ruoyi
 */
@ApiModel(description = "微信手机号请求参数")
public class WxPhoneDTO
{
    @ApiModelProperty(value = "手机号获取凭证code", required = true)
    @NotBlank(message = "code不能为空")
    private String code;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }
}

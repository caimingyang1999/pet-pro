package com.ruoyi.system.domain.dto;

import java.io.Serializable;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户注册请求参数
 *
 * @author ruoyi
 */
@Data
@ApiModel(description = "用户注册请求参数")
public class RegisterDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 手机号 */
    @ApiModelProperty(value = "手机号（11位数字，1开头）", required = true, example = "13812345678")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 密码 */
    @ApiModelProperty(value = "密码（6-20位）", required = true, example = "abc123456")
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度必须为6-20位")
    private String password;

    /** 确认密码 */
    @ApiModelProperty(value = "确认密码（需与密码一致）", required = true, example = "abc123456")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /** 昵称 */
    @ApiModelProperty(value = "用户昵称（选填，默认'宠迹用户'）", example = "小宠")
    private String nickname;

    /**
     * 自定义校验：确认密码必须与密码一致
     *
     * @return 是否一致
     */
    @AssertTrue(message = "两次输入的密码不一致")
    public boolean isPasswordMatch()
    {
        if (password == null || confirmPassword == null)
        {
            return false;
        }
        return password.equals(confirmPassword);
    }
}

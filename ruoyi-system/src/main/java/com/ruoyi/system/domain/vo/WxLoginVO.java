package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 微信登录返回对象
 *
 * @author ruoyi
 */
@ApiModel(description = "微信登录返回对象")
public class WxLoginVO
{
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "登录令牌")
    private String token;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @ApiModelProperty(value = "手机号")
    private String phonenumber;

    @ApiModelProperty(value = "积分余额")
    private Integer points;

    @ApiModelProperty(value = "是否新用户")
    private Boolean isNewUser;

    @ApiModelProperty(value = "登录类型")
    private String loginType;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public Integer getPoints()
    {
        return points;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
    }

    public Boolean getIsNewUser()
    {
        return isNewUser;
    }

    public void setIsNewUser(Boolean isNewUser)
    {
        this.isNewUser = isNewUser;
    }

    public String getLoginType()
    {
        return loginType;
    }

    public void setLoginType(String loginType)
    {
        this.loginType = loginType;
    }
}

package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置属性
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatConfig
{
    /** 小程序AppID */
    private String appid;

    /** 小程序AppSecret */
    private String secret;

    /** 微信接口基础地址 */
    private String baseUrl = "https://api.weixin.qq.com";

    public String getAppid()
    {
        return appid;
    }

    public void setAppid(String appid)
    {
        this.appid = appid;
    }

    public String getSecret()
    {
        return secret;
    }

    public void setSecret(String secret)
    {
        this.secret = secret;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }
}

package com.ruoyi.common.utils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.config.WechatConfig;
import com.ruoyi.common.utils.http.HttpUtils;

/**
 * 微信API调用工具类
 *
 * @author ruoyi
 */
@Component
public class WechatUtils
{
    private static final Logger log = LoggerFactory.getLogger(WechatUtils.class);

    @Resource
    private WechatConfig wechatConfig;

    /**
     * 微信登录（code换openid和session_key）
     *
     * @param code 小程序登录凭证code
     * @return 微信登录结果
     */
    public Map<String, Object> code2Session(String code)
    {
        Map<String, Object> result = new HashMap<>();
        try
        {
            String url = wechatConfig.getBaseUrl() + "/sns/jscode2session";
            Map<String, String> params = new HashMap<>();
            params.put("appid", wechatConfig.getAppid());
            params.put("secret", wechatConfig.getSecret());
            params.put("js_code", code);
            params.put("grant_type", "authorization_code");

            String response = HttpUtils.sendGet(url, buildQueryString(params));
            JSONObject jsonObject = JSON.parseObject(response);

            if (jsonObject.containsKey("errcode"))
            {
                Integer errcode = jsonObject.getInteger("errcode");
                if (errcode != 0)
                {
                    String errmsg = jsonObject.getString("errmsg");
                    log.error("微信登录失败，errcode={}, errmsg={}", errcode, errmsg);
                    result.put("success", false);
                    result.put("message", "微信登录失败：" + errmsg);
                    return result;
                }
            }

            result.put("success", true);
            result.put("openid", jsonObject.getString("openid"));
            result.put("session_key", jsonObject.getString("session_key"));
            result.put("unionid", jsonObject.getString("unionid"));
            result.put("expires_in", jsonObject.getInteger("expires_in"));
        }
        catch (Exception e)
        {
            log.error("微信登录接口调用异常", e);
            result.put("success", false);
            result.put("message", "微信登录接口调用异常");
        }
        return result;
    }

    /**
     * 获取微信手机号
     *
     * @param code 手机号获取凭证code
     * @return 手机号结果
     */
    public Map<String, Object> getPhoneNumber(String code)
    {
        Map<String, Object> result = new HashMap<>();
        try
        {
            String accessToken = getAccessToken();
            if (StringUtils.isEmpty(accessToken))
            {
                result.put("success", false);
                result.put("message", "获取access_token失败");
                return result;
            }

            String url = wechatConfig.getBaseUrl() + "/wxa/business/getuserphonenumber?access_token=" + accessToken;
            Map<String, String> params = new HashMap<>();
            params.put("code", code);

            String response = HttpUtils.sendPost(url, JSON.toJSONString(params));
            JSONObject jsonObject = JSON.parseObject(response);

            if (jsonObject.containsKey("errcode"))
            {
                Integer errcode = jsonObject.getInteger("errcode");
                if (errcode != 0)
                {
                    String errmsg = jsonObject.getString("errmsg");
                    log.error("获取手机号失败，errcode={}, errmsg={}", errcode, errmsg);
                    result.put("success", false);
                    result.put("message", "获取手机号失败：" + errmsg);
                    return result;
                }
            }

            JSONObject phoneInfo = jsonObject.getJSONObject("phone_info");
            result.put("success", true);
            result.put("phoneNumber", phoneInfo.getString("phoneNumber"));
            result.put("purePhoneNumber", phoneInfo.getString("purePhoneNumber"));
            result.put("countryCode", phoneInfo.getInteger("countryCode"));
        }
        catch (Exception e)
        {
            log.error("获取手机号接口调用异常", e);
            result.put("success", false);
            result.put("message", "获取手机号接口调用异常");
        }
        return result;
    }

    /**
     * 获取微信access_token
     *
     * @return access_token
     */
    public String getAccessToken()
    {
        try
        {
            String url = wechatConfig.getBaseUrl() + "/cgi-bin/token";
            Map<String, String> params = new HashMap<>();
            params.put("grant_type", "client_credential");
            params.put("appid", wechatConfig.getAppid());
            params.put("secret", wechatConfig.getSecret());

            String response = HttpUtils.sendGet(url, buildQueryString(params));
            JSONObject jsonObject = JSON.parseObject(response);

            if (jsonObject.containsKey("errcode"))
            {
                Integer errcode = jsonObject.getInteger("errcode");
                if (errcode != 0)
                {
                    log.error("获取access_token失败，errcode={}, errmsg={}", errcode, jsonObject.getString("errmsg"));
                    return null;
                }
            }

            return jsonObject.getString("access_token");
        }
        catch (Exception e)
        {
            log.error("获取access_token异常", e);
            return null;
        }
    }

    /**
     * 将Map转换为URL查询字符串
     *
     * @param params 参数Map
     * @return 查询字符串
     */
    private String buildQueryString(Map<String, String> params)
    {
        if (params == null || params.isEmpty())
        {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if (sb.length() > 0)
            {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}

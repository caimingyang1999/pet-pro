package com.ruoyi.web.service;

import com.ruoyi.system.domain.dto.WxLoginDTO;
import com.ruoyi.system.domain.dto.WxPhoneDTO;
import com.ruoyi.system.domain.dto.WxPhoneLoginDTO;
import com.ruoyi.system.domain.vo.WxLoginVO;

/**
 * 微信用户服务接口
 *
 * @author ruoyi
 */
public interface IWxUserService
{
    /**
     * 微信登录
     *
     * @param loginDTO 登录请求参数
     * @return 登录结果
     */
    WxLoginVO wxLogin(WxLoginDTO loginDTO);

    /**
     * 微信手机号登录
     *
     * @param loginDTO 登录请求参数（code-微信登录凭证，phoneCode-手机号获取凭证）
     * @return 登录结果
     */
    WxLoginVO wxPhoneLogin(WxPhoneLoginDTO loginDTO);

    /**
     * 绑定手机号
     *
     * @param userId 用户ID
     * @param phoneDTO 手机号请求参数
     * @return 操作结果
     */
    boolean bindPhone(Long userId, WxPhoneDTO phoneDTO);
}

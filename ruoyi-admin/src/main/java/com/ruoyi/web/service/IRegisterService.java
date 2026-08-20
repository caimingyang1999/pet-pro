package com.ruoyi.web.service;

import com.ruoyi.system.domain.dto.RegisterDTO;
import com.ruoyi.system.domain.vo.RegisterVO;

/**
 * 小程序用户注册 业务层
 *
 * @author ruoyi
 */
public interface IRegisterService
{
    /**
     * 用户注册（手机号+密码）
     *
     * @param registerDTO 注册参数
     * @return 注册结果（包含 token、userId、nickname、phone、points）
     */
    RegisterVO register(RegisterDTO registerDTO);
}

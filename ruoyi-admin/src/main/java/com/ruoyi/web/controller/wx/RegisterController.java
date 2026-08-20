package com.ruoyi.web.controller.wx;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.dto.RegisterDTO;
import com.ruoyi.system.domain.vo.RegisterVO;
import com.ruoyi.web.service.IRegisterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 小程序用户注册（手机号+密码）
 *
 * @author ruoyi
 */
@Api(tags = "用户注册")
@RestController
@RequestMapping("/api/v1")
public class RegisterController extends BaseController
{
    @Resource
    private IRegisterService registerService;

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数（phone-手机号 password-密码 confirmPassword-确认密码 nickname-昵称）
     * @return 注册结果（token-令牌 userId-用户ID nickname-昵称 phone-脱敏手机号 points-积分）
     */
    @ApiOperation("用户注册（手机号+密码）")
    @Anonymous
    @PostMapping("/register")
    public AjaxResult register(@Validated @RequestBody RegisterDTO registerDTO)
    {
        RegisterVO vo = registerService.register(registerDTO);
        return success(vo);
    }
}

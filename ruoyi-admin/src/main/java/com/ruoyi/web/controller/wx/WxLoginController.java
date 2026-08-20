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
import com.ruoyi.system.domain.dto.WxLoginDTO;
import com.ruoyi.system.domain.dto.WxPhoneDTO;
import com.ruoyi.system.domain.dto.WxPhoneLoginDTO;
import com.ruoyi.system.domain.vo.WxLoginVO;
import com.ruoyi.web.service.IWxUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 微信登录控制器
 *
 * @author ruoyi
 */
@Api(tags = "微信小程序登录")
@RestController
@RequestMapping("/api/v1/wx")
public class WxLoginController extends BaseController
{
    @Resource
    private IWxUserService wxUserService;

    /**
     * 微信登录
     *
     * @param loginDTO 登录请求参数（code-微信登录凭证，nickName-昵称，avatar-头像，gender-性别）
     * @return 登录结果（token-令牌，userId-用户ID，nickName-昵称，avatar-头像，phonenumber-手机号，points-积分，isNewUser-是否新用户）
     */
    @ApiOperation("微信登录")
    @Anonymous
    @PostMapping("/login")
    public AjaxResult wxLogin(@Validated @RequestBody WxLoginDTO loginDTO)
    {
        WxLoginVO vo = wxUserService.wxLogin(loginDTO);
        return AjaxResult.success(vo);
    }

    /**
     * 微信手机号登录
     *
     * @param loginDTO 登录请求参数（code-微信登录凭证，phoneCode-手机号获取凭证）
     * @return 登录结果（token-令牌，userId-用户ID，nickName-昵称，phonenumber-手机号，points-积分，isNewUser-是否新用户）
     */
    @ApiOperation("微信手机号登录")
    @Anonymous
    @PostMapping("/phone-login")
    public AjaxResult wxPhoneLogin(@Validated @RequestBody WxPhoneLoginDTO loginDTO)
    {
        WxLoginVO vo = wxUserService.wxPhoneLogin(loginDTO);
        return AjaxResult.success(vo);
    }

    /**
     * 绑定手机号
     *
     * @param phoneDTO 手机号请求参数（code-手机号获取凭证）
     * @return 操作结果
     */
    @ApiOperation("绑定手机号")
    @PostMapping("/bind-phone")
    public AjaxResult bindPhone(@Validated @RequestBody WxPhoneDTO phoneDTO)
    {
        return toAjax(wxUserService.bindPhone(getUserId(), phoneDTO));
    }
}

package com.ruoyi.web.controller.user;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.domain.UserAddress;
import com.ruoyi.system.domain.UserPointsLog;
import com.ruoyi.system.domain.vo.UserInfoVO;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.IUserAddressService;
import com.ruoyi.system.service.IUserPointsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 用户中心 控制器
 *
 * @author ruoyi
 */
@Api(tags = "用户中心")
@RestController
@RequestMapping("/api/v1/user")
public class UserController extends BaseController
{
    @Resource
    private IUserPointsService userPointsService;

    @Resource
    private IUserAddressService userAddressService;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private TokenService tokenService;

    @Resource
    private ISysUserService sysUserService;

    /**
     * 用户账号密码登录（无需验证码）
     *
     * @param loginBody 登录信息（username、password）
     * @return token
     */
    @ApiOperation("用户账号密码登录")
    @PostMapping("/uertlogin")
    public AjaxResult uertlogin(@RequestBody LoginBody loginBody)
    {
        String phone = loginBody.getUsername();
        String password = loginBody.getPassword();
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(password))
        {
            throw new ServiceException("手机号或密码不能为空");
        }

        // 根据手机号查询用户，获取真实用户名用于认证
        SysUser sysUser = sysUserMapper.selectUserByPhonenumber(phone);
        if (sysUser == null)
        {
            throw new ServiceException("用户不存在");
        }
        String realUsername = sysUser.getUserName();

        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(realUsername, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                throw new UserPasswordNotMatchException();
            }
            else
            {
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        sysUserService.updateLoginInfo(loginUser.getUserId(), IpUtils.getIpAddr(), DateUtils.getNowDate());
        String token = tokenService.createToken(loginUser);

        AjaxResult ajax = AjaxResult.success();
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息（userId、nickName、avatar、phonenumber、points、loginType）
     */
    @ApiOperation("获取当前用户信息")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/info")
    public AjaxResult getInfo()
    {
        Long userId = getUserId();
        SysUser user = sysUserMapper.selectUserById(userId);
        if (user == null)
        {
            throw new ServiceException("用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setNickName(user.getNickName());
        vo.setAvatar(user.getAvatar());
        vo.setPhonenumber(user.getPhonenumber());
        vo.setPoints(user.getPoints() != null ? user.getPoints() : 0);
        vo.setLoginType(user.getLoginType());
        return AjaxResult.success(vo);
    }

    /**
     * 积分明细
     *
     * @return 积分变动记录分页列表
     */
    @ApiOperation("积分明细")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/points/log")
    public TableDataInfo pointsLog()
    {
        startPage();
        List<UserPointsLog> list = userPointsService.getPointsLog(getUserId());
        return getDataTable(list);
    }

    /**
     * 地址列表
     *
     * @return 当前用户收货地址列表
     */
    @ApiOperation("地址列表")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/addresses")
    public AjaxResult addressList()
    {
        List<UserAddress> list = userAddressService.getAddressList(getUserId());
        return AjaxResult.success(list);
    }

    /**
     * 添加地址
     *
     * @param address 地址信息（receiverName-收件人，receiverPhone-电话，detailAddress-详细地址，isDefault-是否默认）
     * @return 操作结果
     */
    @ApiOperation("添加地址")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping("/addresses")
    public AjaxResult addAddress(@Validated @RequestBody UserAddress address)
    {
        address.setUserId(getUserId());
        return toAjax(userAddressService.addAddress(address));
    }

    /**
     * 更新地址
     *
     * @param id      地址ID
     * @param address 地址信息
     * @return 操作结果
     */
    @ApiOperation("更新地址")
    @PreAuthorize("@ss.isAuthenticated()")
    @PutMapping("/addresses/{id}")
    public AjaxResult updateAddress(
            @ApiParam(name = "id", value = "地址ID", required = true)
            @PathVariable Long id,
            @RequestBody UserAddress address)
    {
        address.setId(id);
        return toAjax(userAddressService.updateAddress(address, getUserId()));
    }

    /**
     * 删除地址
     *
     * @param id 地址ID
     * @return 操作结果
     */
    @ApiOperation("删除地址")
    @PreAuthorize("@ss.isAuthenticated()")
    @DeleteMapping("/addresses/{id}")
    public AjaxResult deleteAddress(
            @ApiParam(name = "id", value = "地址ID", required = true)
            @PathVariable Long id)
    {
        return toAjax(userAddressService.deleteAddress(id, getUserId()));
    }
}

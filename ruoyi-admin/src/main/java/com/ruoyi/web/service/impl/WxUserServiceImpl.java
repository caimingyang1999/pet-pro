package com.ruoyi.web.service.impl;

import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.WechatUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.domain.dto.WxLoginDTO;
import com.ruoyi.system.domain.dto.WxPhoneDTO;
import com.ruoyi.system.domain.dto.WxPhoneLoginDTO;
import com.ruoyi.system.domain.vo.WxLoginVO;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.IUserPointsService;
import com.ruoyi.web.service.IWxUserService;

/**
 * 微信用户服务实现
 *
 * @author ruoyi
 */
@Service
public class WxUserServiceImpl implements IWxUserService
{
    private static final Logger log = LoggerFactory.getLogger(WxUserServiceImpl.class);

    @Resource
    private WechatUtils wechatUtils;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private TokenService tokenService;

    @Resource
    private SysPermissionService permissionService;

    @Resource
    private ISysUserService userService;

    @Resource
    private IUserPointsService userPointsService;

    /** 注册赠送积分 */
    private static final int REGISTER_REWARD_POINTS = 20;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxLoginVO wxLogin(WxLoginDTO loginDTO)
    {
        WxLoginVO result = new WxLoginVO();

        Map<String, Object> wxResult = wechatUtils.code2Session(loginDTO.getCode());
        if (!Boolean.TRUE.equals(wxResult.get("success")))
        {
            throw new ServiceException((String) wxResult.get("message"));
        }

        String openid = (String) wxResult.get("openid");
        String unionid = (String) wxResult.get("unionid");

        SysUser user = sysUserMapper.selectUserByOpenId(openid);
        boolean isNewUser = false;

        if (user == null)
        {
            isNewUser = true;
            user = createWxUser(openid, unionid, loginDTO);
            log.info("微信新用户注册，openid={}, userId={}", openid, user.getUserId());
        }
        else
        {
            log.info("微信用户登录，openid={}, userId={}", openid, user.getUserId());
            updateUserInfo(user, loginDTO);
        }

        if ("1".equals(user.getDelFlag()))
        {
            throw new ServiceException("该账号已被删除");
        }
        if ("1".equals(user.getStatus()))
        {
            throw new ServiceException("该账号已被停用");
        }

        String token = generateToken(user);

        result.setUserId(user.getUserId());
        result.setToken(token);
        result.setNickName(user.getNickName());
        result.setAvatar(user.getAvatar());
        result.setPhonenumber(user.getPhonenumber());
        result.setPoints(user.getPoints() != null ? user.getPoints() : 0);
        result.setIsNewUser(isNewUser);
        result.setLoginType("wx");

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxLoginVO wxPhoneLogin(WxPhoneLoginDTO loginDTO)
    {
        WxLoginVO result = new WxLoginVO();

        // 1. 微信登录 code 换取 openid
        Map<String, Object> wxResult = wechatUtils.code2Session(loginDTO.getCode());
        if (!Boolean.TRUE.equals(wxResult.get("success")))
        {
            throw new ServiceException((String) wxResult.get("message"));
        }
        String openid = (String) wxResult.get("openid");
        String unionid = (String) wxResult.get("unionid");

        // 2. 手机号凭证 code 换取手机号
        Map<String, Object> phoneResult = wechatUtils.getPhoneNumber(loginDTO.getPhoneCode());
        if (!Boolean.TRUE.equals(phoneResult.get("success")))
        {
            throw new ServiceException((String) phoneResult.get("message"));
        }
        // 优先使用无区号的纯手机号，避免与注册时存储的 11 位号码格式不一致
        String phoneNumber = (String) phoneResult.get("purePhoneNumber");
        if (StringUtils.isEmpty(phoneNumber))
        {
            phoneNumber = (String) phoneResult.get("phoneNumber");
        }

        // 3. 按手机号匹配用户：存在则绑定微信并登录，不存在则创建新用户
        SysUser user = sysUserMapper.selectUserByPhonenumber(phoneNumber);
        boolean isNewUser = false;
        if (user == null)
        {
            isNewUser = true;
            user = createWxPhoneUser(openid, unionid, phoneNumber);
            log.info("微信手机号新用户注册，phone={}, userId={}", phoneNumber, user.getUserId());
        }
        else
        {
            // 手机号老用户首次微信登录：绑定 openid，之后可直接微信登录
            sysUserMapper.updateWxLoginInfo(user.getUserId(), openid, unionid, "wx");
            log.info("微信手机号登录，phone={}, userId={}", phoneNumber, user.getUserId());
        }

        if ("1".equals(user.getDelFlag()))
        {
            throw new ServiceException("该账号已被删除");
        }
        if ("1".equals(user.getStatus()))
        {
            throw new ServiceException("该账号已被停用");
        }

        String token = generateToken(user);

        result.setUserId(user.getUserId());
        result.setToken(token);
        result.setNickName(user.getNickName());
        result.setAvatar(user.getAvatar());
        result.setPhonenumber(user.getPhonenumber());
        result.setPoints(user.getPoints() != null ? user.getPoints() : 0);
        result.setIsNewUser(isNewUser);
        result.setLoginType("wx");

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindPhone(Long userId, WxPhoneDTO phoneDTO)
    {
        Map<String, Object> phoneResult = wechatUtils.getPhoneNumber(phoneDTO.getCode());
        if (!Boolean.TRUE.equals(phoneResult.get("success")))
        {
            throw new ServiceException((String) phoneResult.get("message"));
        }

        String phoneNumber = (String) phoneResult.get("phoneNumber");
        SysUser user = sysUserMapper.selectUserById(userId);
        if (user == null)
        {
            throw new ServiceException("用户不存在");
        }

        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !user.getPhonenumber().equals(phoneNumber))
        {
            SysUser existUser = sysUserMapper.selectUserByPhonenumber(phoneNumber);
            if (existUser != null && !existUser.getUserId().equals(userId))
            {
                throw new ServiceException("该手机号已被其他账号绑定");
            }
        }

        user.setPhonenumber(phoneNumber);
        user.setUpdateBy("wx_miniapp");
        return sysUserMapper.updateUser(user) > 0;
    }

    private SysUser createWxUser(String openid, String unionid, WxLoginDTO loginDTO)
    {
        SysUser user = new SysUser();
        String userName = "wx_" + IdUtils.fastUUID().substring(0, 8);
        user.setUserName(userName);
        user.setNickName(StringUtils.isNotEmpty(loginDTO.getNickName()) ? loginDTO.getNickName() : "微信用户");
        user.setAvatar(loginDTO.getAvatar());
        user.setPassword(null);
        user.setStatus("0");
        user.setPoints(0);
        user.setOpenId(openid);
        user.setUnionId(unionid);
        user.setLoginType("wx");
        user.setSex(loginDTO.getGender());

        sysUserMapper.insertWxUser(user);

        if (user.getUserId() == null)
        {
            throw new ServiceException("微信用户创建失败");
        }

        try
        {
            userService.insertUserAuth(user.getUserId(), new Long[]{ 2L });
        }
        catch (Exception e)
        {
            log.warn("为微信用户分配默认角色失败，userId={}", user.getUserId(), e);
        }

        // 发放注册奖励积分（记录积分变动日志）
        userPointsService.addPoints(user.getUserId(), REGISTER_REWARD_POINTS, "register", null);

        return sysUserMapper.selectUserById(user.getUserId());
    }

    private SysUser createWxPhoneUser(String openid, String unionid, String phoneNumber)
    {
        SysUser user = new SysUser();
        String userName = "wx_" + IdUtils.fastUUID().substring(0, 8);
        user.setUserName(userName);
        user.setNickName("微信用户");
        user.setPhonenumber(phoneNumber);
        user.setPassword(null);
        user.setStatus("0");
        user.setPoints(0);
        user.setOpenId(openid);
        user.setUnionId(unionid);
        user.setLoginType("wx");

        sysUserMapper.insertWxUser(user);

        if (user.getUserId() == null)
        {
            throw new ServiceException("微信用户创建失败");
        }

        try
        {
            userService.insertUserAuth(user.getUserId(), new Long[]{ 2L });
        }
        catch (Exception e)
        {
            log.warn("为微信用户分配默认角色失败，userId={}", user.getUserId(), e);
        }

        // 发放注册奖励积分（记录积分变动日志）
        userPointsService.addPoints(user.getUserId(), REGISTER_REWARD_POINTS, "register", null);

        return sysUserMapper.selectUserById(user.getUserId());
    }

    private void updateUserInfo(SysUser user, WxLoginDTO loginDTO)
    {
        boolean needUpdate = false;
        if (StringUtils.isNotEmpty(loginDTO.getNickName()) && !loginDTO.getNickName().equals(user.getNickName()))
        {
            user.setNickName(loginDTO.getNickName());
            needUpdate = true;
        }
        if (StringUtils.isNotEmpty(loginDTO.getAvatar()) && !loginDTO.getAvatar().equals(user.getAvatar()))
        {
            user.setAvatar(loginDTO.getAvatar());
            needUpdate = true;
        }
        if (StringUtils.isNotEmpty(loginDTO.getGender()) && !loginDTO.getGender().equals(user.getSex()))
        {
            user.setSex(loginDTO.getGender());
            needUpdate = true;
        }
        if (needUpdate)
        {
            user.setUpdateBy("wx_miniapp");
            sysUserMapper.updateUser(user);
        }

        if (StringUtils.isEmpty(user.getLoginType()))
        {
            sysUserMapper.updateWxLoginInfo(user.getUserId(), user.getOpenId(), user.getUnionId(), "wx");
        }
    }

    private String generateToken(SysUser user)
    {
        LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user,
                permissionService.getMenuPermission(user));
        return tokenService.createToken(loginUser);
    }
}

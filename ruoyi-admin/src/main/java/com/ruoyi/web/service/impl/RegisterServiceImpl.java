package com.ruoyi.web.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.domain.dto.RegisterDTO;
import com.ruoyi.system.domain.vo.RegisterVO;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.web.service.IRegisterService;

/**
 * 小程序用户注册 业务层实现
 *
 * @author ruoyi
 *
 * 说明：该实现位于 ruoyi-admin 模块，因为需要依赖 ruoyi-framework 的
 * TokenService 和 SysPermissionService 来生成 JWT。ruoyi-system 模块
 * 不依赖 ruoyi-framework，避免循环依赖。
 */
@Service
public class RegisterServiceImpl implements IRegisterService
{
    private static final Logger log = LoggerFactory.getLogger(RegisterServiceImpl.class);

    /** 普通用户角色标识 */
    private static final String COMMON_ROLE_KEY = "common";

    /** 注册赠送积分 */
    private static final int REGISTER_REWARD_POINTS = 20;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private SysRoleMapper roleMapper;

    @Resource
    private SysUserRoleMapper userRoleMapper;

    @Resource
    private TokenService tokenService;

    @Resource
    private SysPermissionService permissionService;

    /**
     * 用户注册（手机号+密码）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterVO register(RegisterDTO registerDTO)
    {
        long start = System.currentTimeMillis();
        String maskedPhone = maskPhone(registerDTO.getPhone());

        try
        {
            // 1. 参数基础校验
            validateBasic(registerDTO);

            // 2. 手机号唯一性校验
            SysUser exist = userMapper.selectUserByPhonenumber(registerDTO.getPhone());
            if (exist != null)
            {
                if ("wechat_miniapp".equals(exist.getLoginType()) || "wx".equals(exist.getLoginType()))
                {
                    throw new ServiceException("该手机号已绑定微信，请使用微信登录");
                }
                throw new ServiceException("该手机号已注册，请直接登录");
            }

            // 3. 创建用户
            SysUser newUser = buildNewUser(registerDTO);
            int rows = userMapper.insertSysUser(newUser);
            if (rows <= 0 || newUser.getUserId() == null)
            {
                throw new ServiceException("注册失败，请稍后重试");
            }

            // 4. 分配普通用户角色
            assignCommonRole(newUser.getUserId());

            // 5. 重新查询用户完整信息（含角色）
            SysUser fullUser = userMapper.selectUserById(newUser.getUserId());
            if (fullUser == null)
            {
                throw new ServiceException("注册失败，请稍后重试");
            }

            // 6. 生成 Token
            String token = generateToken(fullUser);

            // 7. 构造返回结果
            RegisterVO vo = new RegisterVO();
            vo.setToken(token);
            vo.setUserId(fullUser.getUserId());
            vo.setNickname(fullUser.getNickName());
            vo.setPhone(maskedPhone);
            vo.setPoints(fullUser.getPoints() == null ? 0 : fullUser.getPoints());

            log.info("注册成功 phone={}, userId={}, 耗时={}ms", maskedPhone, fullUser.getUserId(),
                    System.currentTimeMillis() - start);
            return vo;
        }
        catch (ServiceException e)
        {
            log.warn("注册失败 phone={}, reason={}", maskedPhone, e.getMessage());
            throw e;
        }
        catch (Exception e)
        {
            log.error("注册异常 phone={}, 耗时={}ms", maskedPhone, System.currentTimeMillis() - start, e);
            throw new ServiceException("注册失败，请稍后重试");
        }
    }

    /**
     * 基础参数校验
     */
    private void validateBasic(RegisterDTO dto)
    {
        if (dto == null)
        {
            throw new ServiceException("注册参数不能为空");
        }
        if (StringUtils.isEmpty(dto.getPhone()))
        {
            throw new ServiceException("手机号不能为空");
        }
        if (!dto.getPhone().matches("^1[3-9]\\d{9}$"))
        {
            throw new ServiceException("手机号格式不正确");
        }
        if (StringUtils.isEmpty(dto.getPassword()))
        {
            throw new ServiceException("密码不能为空");
        }
        if (dto.getPassword().length() < 6 || dto.getPassword().length() > 20)
        {
            throw new ServiceException("密码长度必须为6-20位");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
        {
            throw new ServiceException("两次输入的密码不一致");
        }
    }

    /**
     * 构造新用户对象
     */
    private SysUser buildNewUser(RegisterDTO dto)
    {
        SysUser user = new SysUser();
        user.setUserName(generateUniqueUserName());
        user.setNickName(StringUtils.isNotEmpty(dto.getNickname()) ? dto.getNickname() : "宠迹用户");
        user.setPhonenumber(dto.getPhone());
        user.setPassword(SecurityUtils.encryptPassword(dto.getPassword()));
        user.setStatus("0");
        user.setDelFlag("0");
        user.setPoints(REGISTER_REWARD_POINTS);
        user.setLoginType("sys");
        return user;
    }

    /**
     * 生成唯一用户名
     */
    private String generateUniqueUserName()
    {
        String userName;
        int tryCount = 0;
        do
        {
            userName = "user_" + System.currentTimeMillis() + RandomUtils.nextInt(1000, 9999);
            tryCount++;
            if (tryCount > 10)
            {
                userName = "user_" + System.currentTimeMillis() + RandomUtils.nextInt(10000, 99999);
                break;
            }
        }
        while (userMapper.checkUserNameUnique(userName) != null);
        return userName;
    }

    /**
     * 为用户分配普通用户角色
     */
    private void assignCommonRole(Long userId)
    {
        SysRole role = roleMapper.checkRoleKeyUnique(COMMON_ROLE_KEY);
        if (role == null || role.getRoleId() == null)
        {
            log.warn("未找到角色标识为'{}'的普通用户角色，请先在sys_role表中初始化该角色", COMMON_ROLE_KEY);
            return;
        }

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getRoleId());

        List<SysUserRole> list = new ArrayList<>();
        list.add(userRole);
        userRoleMapper.batchUserRole(list);
    }

    /**
     * 生成 Token
     */
    private String generateToken(SysUser user)
    {
        Set<String> permissions = permissionService.getMenuPermission(user);
        LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, permissions);
        return tokenService.createToken(loginUser);
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone)
    {
        if (StringUtils.isEmpty(phone) || phone.length() < 7)
        {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}

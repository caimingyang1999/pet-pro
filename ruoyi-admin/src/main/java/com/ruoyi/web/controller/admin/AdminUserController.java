package com.ruoyi.web.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.PetInfo;
import com.ruoyi.system.domain.PetPost;
import com.ruoyi.system.domain.UserPointsLog;
import com.ruoyi.system.service.IPetInfoService;
import com.ruoyi.system.service.IPostService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.IUserPointsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台-用户管理 控制器
 *
 * @author ruoyi
 */
@Api(tags = "后台-用户管理")
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("@ss.hasPermi('admin')")
public class AdminUserController extends BaseController
{
    @Resource
    private ISysUserService sysUserService;

    @Resource
    private IPetInfoService petInfoService;

    @Resource
    private IPostService postService;

    @Resource
    private IUserPointsService userPointsService;

    /**
     * 用户列表（含宠物数量、动态数量、积分）
     *
     * @param userName 用户账号
     * @param phonenumber 手机号码
     * @return 用户分页列表
     */
    @ApiOperation("用户列表")
    @Log(title = "用户管理", businessType = BusinessType.OTHER)
    @GetMapping
    public TableDataInfo list(
            @ApiParam(name = "userName", value = "用户账号") @RequestParam(required = false) String userName,
            @ApiParam(name = "phonenumber", value = "手机号码") @RequestParam(required = false) String phonenumber)
    {
        startPage();
        SysUser query = new SysUser();
        query.setUserName(userName);
        query.setPhonenumber(phonenumber);
        List<SysUser> list = sysUserService.selectUserList(query);
        long total = new PageInfo<>(list).getTotal();

        // 补充宠物数量、动态数量（积分已存在于 SysUser.points）
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SysUser user : list)
        {
            Map<String, Object> row = new HashMap<>();
            row.put("user", user);
            row.put("petCount", petInfoService.count(new LambdaQueryWrapper<PetInfo>().eq(PetInfo::getUserId, user.getUserId())));
            row.put("postCount", postService.count(new LambdaQueryWrapper<PetPost>().eq(PetPost::getUserId, user.getUserId())));
            row.put("points", user.getPoints());
            rows.add(row);
        }

        TableDataInfo table = new TableDataInfo();
        table.setCode(HttpStatus.SUCCESS);
        table.setMsg("查询成功");
        table.setRows(rows);
        table.setTotal(total);
        return table;
    }

    /**
     * 积分操作（增加/扣减）
     *
     * @param userId 用户ID
     * @param log    积分信息（pointsChange-变动积分，正数增加负数扣减；remark-备注）
     * @return 操作结果
     */
    @ApiOperation("积分操作")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/{userId}/points")
    public AjaxResult points(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @PathVariable Long userId,
            @RequestBody UserPointsLog log)
    {
        if (log.getPointsChange() == null || log.getPointsChange() == 0)
        {
            return AjaxResult.error("积分变动值不能为空或0");
        }
        // type=admin，relateId 记录操作管理员ID
        return toAjax(userPointsService.addPoints(userId, log.getPointsChange(), "admin", getUserId()));
    }

    /**
     * 用户积分明细列表
     *
     * @param userId 用户ID
     * @return 积分变动记录分页列表
     */
    @ApiOperation("用户积分明细")
    @Log(title = "用户管理", businessType = BusinessType.OTHER)
    @GetMapping("/{userId}/points/records")
    public TableDataInfo pointsRecords(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @PathVariable Long userId)
    {
        startPage();
        List<UserPointsLog> list = userPointsService.getPointsLog(userId);
        return getDataTable(list);
    }
}

package com.ruoyi.system.domain.dto;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 动态查询参数
 *
 * @author ruoyi
 */
@Data
@ApiModel("动态查询参数")
public class PostQueryDTO
{
    /** 关键词（搜索动态内容/用户名） */
    @ApiModelProperty("关键词")
    private String keyword;

    /** 发布者用户ID（查某个人的动态） */
    @ApiModelProperty("发布者用户ID")
    private Long userId;

    /** 列表 Tab: recommend-推荐 follow-关注 latest-最新 */
    @ApiModelProperty("列表Tab：recommend-推荐 follow-关注 latest-最新")
    private String tab;

    /** 当前登录用户ID（用于：1.关注列表过滤 2.点赞状态回显） */
    @ApiModelProperty("当前登录用户ID")
    private Long currentUserId;

    /** 关注列表的被关注者ID集合（用于关注tab动态筛选） */
    @ApiModelProperty("关注列表的被关注者ID集合")
    private List<Long> followeeIds;

    /** 当前页码 */
    @ApiModelProperty("当前页码")
    private Integer pageNum = 1;

    /** 每页条数 */
    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;
}

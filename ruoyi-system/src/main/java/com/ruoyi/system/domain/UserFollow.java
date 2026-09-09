package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户关注关系表 user_follow
 *
 * @author ruoyi
 */
@Data
@TableName("user_follow")
public class UserFollow implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关注者用户ID */
    @ApiModelProperty("关注者用户ID")
    private Long followerId;

    /** 被关注者用户ID */
    @ApiModelProperty("被关注者用户ID")
    private Long followeeId;

    /** 删除标志（0-存在 2-已删除） */
    @ApiModelProperty("删除标志")
    private String delFlag;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // ===== 非持久化字段（关联查询，Mapper XML 填充） =====

    /** 被关注者昵称 */
    @ApiModelProperty("被关注者昵称")
    @TableField(exist = false)
    private String followeeNickName;

    /** 被关注者头像 */
    @ApiModelProperty("被关注者头像")
    @TableField(exist = false)
    private String followeeAvatar;

    /** 关注者昵称 */
    @ApiModelProperty("关注者昵称")
    @TableField(exist = false)
    private String followerNickName;

    /** 关注者头像 */
    @ApiModelProperty("关注者头像")
    @TableField(exist = false)
    private String followerAvatar;
}

package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 动态表 pet_post
 *
 * @author ruoyi
 */
@Data
@TableName("pet_post")
public class PetPost implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布用户ID */
    @ApiModelProperty("发布用户ID")
    private Long userId;

    /** 关联宠物ID */
    @ApiModelProperty("关联宠物ID")
    private Long petId;

    /** 动态内容 */
    @ApiModelProperty("动态内容")
    private String content;

    /** 图片列表（JSON数组，以字符串形式存储） */
    @ApiModelProperty("图片列表（JSON数组，以字符串形式存储）")
    private String images;

    /** 审核状态（0-待审核 1-通过 2-拒绝） */
    @ApiModelProperty("审核状态（0-待审核 1-通过 2-拒绝）")
    private String status;

    /** 点赞数 */
    @ApiModelProperty("点赞数")
    private Integer likeCount;

    /** 评论数 */
    @ApiModelProperty("评论数")
    private Integer commentCount;

    /** 浏览数 */
    @ApiModelProperty("浏览数")
    private Integer viewCount;

    /** 删除标志 */
    @ApiModelProperty("删除标志")
    @TableLogic
    private String delFlag;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 发布者用户名（关联查询） */
    @ApiModelProperty("发布者用户名")
    @TableField(exist = false)
    private String userName;

    /** 发布者头像（关联查询） */
    @ApiModelProperty("发布者头像")
    @TableField(exist = false)
    private String userAvatar;

    /** 关联宠物名称（关联查询） */
    @ApiModelProperty("关联宠物名称")
    @TableField(exist = false)
    private String petName;
}

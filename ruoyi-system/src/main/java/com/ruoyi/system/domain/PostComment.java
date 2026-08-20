package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 动态评论表 post_comment
 *
 * @author ruoyi
 */
@Data
@TableName("post_comment")
public class PostComment implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 动态ID */
    @ApiModelProperty("动态ID")
    private Long postId;

    /** 评论用户ID */
    @ApiModelProperty("评论用户ID")
    private Long userId;

    /** 父评论ID（0-一级评论） */
    @ApiModelProperty("父评论ID（0-一级评论）")
    private Long parentId;

    /** 评论内容 */
    @ApiModelProperty("评论内容")
    private String content;

    /** 删除标志 */
    @ApiModelProperty("删除标志")
    @TableLogic
    private String delFlag;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 子评论列表（树形结构） */
    @ApiModelProperty("子评论列表")
    @TableField(exist = false)
    private List<PostComment> children;

    /** 评论者用户名（关联查询） */
    @ApiModelProperty("评论者用户名")
    @TableField(exist = false)
    private String userName;

    /** 评论者头像（关联查询） */
    @ApiModelProperty("评论者头像")
    @TableField(exist = false)
    private String userAvatar;
}

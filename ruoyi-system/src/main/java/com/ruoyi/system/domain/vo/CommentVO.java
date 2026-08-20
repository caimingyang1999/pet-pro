package com.ruoyi.system.domain.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 评论返回对象
 *
 * @author ruoyi
 */
@Data
@ApiModel("评论返回对象")
public class CommentVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("评论ID")
    private Long id;

    @ApiModelProperty("动态ID")
    private Long postId;

    @ApiModelProperty("评论用户ID")
    private Long userId;

    @ApiModelProperty("评论者用户名")
    private String userName;

    @ApiModelProperty("评论者头像")
    private String userAvatar;

    @ApiModelProperty("父评论ID")
    private Long parentId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("子评论列表")
    private List<CommentVO> children;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}

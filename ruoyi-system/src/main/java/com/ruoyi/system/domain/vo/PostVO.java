package com.ruoyi.system.domain.vo;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 动态返回对象
 *
 * @author ruoyi
 */
@Data
@ApiModel("动态返回对象")
public class PostVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("动态ID")
    private Long id;

    @ApiModelProperty("发布用户ID")
    private Long userId;

    @ApiModelProperty("发布者用户名")
    private String userName;

    @ApiModelProperty("发布者头像")
    private String userAvatar;

    @ApiModelProperty("关联宠物ID")
    private Long petId;

    @ApiModelProperty("宠物名")
    private String petName;

    @ApiModelProperty("动态内容")
    private String content;

    @ApiModelProperty("图片列表")
    private String images;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("评论数")
    private Integer commentCount;

    @ApiModelProperty("浏览数")
    private Integer viewCount;

    @ApiModelProperty("是否已点赞")
    private Boolean liked;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}

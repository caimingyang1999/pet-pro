package com.ruoyi.system.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 发表评论参数
 *
 * @author ruoyi
 */
@Data
@ApiModel("发表评论参数")
public class CommentCreateDTO
{
    /** 动态ID */
    @ApiModelProperty("动态ID")
    private Long postId;

    /** 评论内容 */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500个字符")
    @ApiModelProperty("评论内容")
    private String content;

    /** 父评论ID（默认0） */
    @ApiModelProperty("父评论ID（默认0）")
    private Long parentId = 0L;
}

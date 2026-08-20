package com.ruoyi.system.domain.dto;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 轮播图新增/编辑请求参数
 *
 * @author ruoyi
 */
@Data
@ApiModel("轮播图新增/编辑请求参数")
public class BannerDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID（编辑时必填）")
    private Long id;

    /** 轮播图标题 */
    @ApiModelProperty("轮播图标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 图片地址 */
    @ApiModelProperty("图片地址")
    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    /** 跳转类型（none-不跳转/post-动态详情/product-商品详情/url-外部链接/miniapp-小程序页面） */
    @ApiModelProperty("跳转类型")
    @NotBlank(message = "跳转类型不能为空")
    private String jumpType;

    /** 跳转目标（根据jump_type存储对应的ID或URL） */
    @ApiModelProperty("跳转目标")
    private String jumpTarget;

    /** 排序号（数字越小越靠前） */
    @ApiModelProperty("排序号")
    private Integer sortOrder;

    /** 状态（0-停用 1-启用） */
    @ApiModelProperty("状态（0-停用 1-启用）")
    private String status;

    /** 展示开始时间 */
    @ApiModelProperty("展示开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 展示结束时间 */
    @ApiModelProperty("展示结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 备注 */
    @ApiModelProperty("备注")
    private String remark;

    /** 当前页码（后台分页查询用） */
    @ApiModelProperty("当前页码")
    private Integer pageNum = 1;

    /** 每页条数（后台分页查询用） */
    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;
}

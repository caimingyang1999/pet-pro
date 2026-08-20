package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 轮播图表 banner
 *
 * @author ruoyi
 */
@Data
@TableName("banner")
public class Banner implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 轮播图标题 */
    @ApiModelProperty("轮播图标题")
    private String title;

    /** 图片地址 */
    @ApiModelProperty("图片地址")
    private String imageUrl;

    /** 跳转类型（none-不跳转/post-动态详情/product-商品详情/url-外部链接/miniapp-小程序页面） */
    @ApiModelProperty("跳转类型")
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

    /** 创建人 */
    @ApiModelProperty("创建人")
    private String createBy;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新人 */
    @ApiModelProperty("更新人")
    private String updateBy;

    /** 更新时间 */
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 删除标志（0-存在 1-删除） */
    @ApiModelProperty("删除标志")
    @TableLogic
    private String delFlag;
}

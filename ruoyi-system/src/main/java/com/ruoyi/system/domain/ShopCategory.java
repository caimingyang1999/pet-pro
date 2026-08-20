package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品分类表 shop_category
 *
 * @author ruoyi
 */
@Data
@TableName("shop_category")
public class ShopCategory implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父分类ID */
    @ApiModelProperty("父分类ID")
    private Long parentId;

    /** 分类名称 */
    @ApiModelProperty("分类名称")
    private String categoryName;

    /** 分类图标 */
    @ApiModelProperty("分类图标")
    private String icon;

    /** 排序 */
    @ApiModelProperty("排序")
    private Integer sortOrder;

    /** 状态（0-停用 1-正常） */
    @ApiModelProperty("状态（0-停用 1-正常）")
    private String status;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 子分类列表（树形结构） */
    @ApiModelProperty("子分类列表")
    @TableField(exist = false)
    private List<ShopCategory> children;
}

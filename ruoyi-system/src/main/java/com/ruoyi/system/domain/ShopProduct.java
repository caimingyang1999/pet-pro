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
 * 商品表 shop_product
 *
 * @author ruoyi
 */
@Data
@TableName("shop_product")
public class ShopProduct implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类ID */
    @ApiModelProperty("分类ID")
    private Long categoryId;

    /** 商品名称 */
    @ApiModelProperty("商品名称")
    private String productName;

    /** 商品图片（多个URL以逗号分隔） */
    @ApiModelProperty("商品图片（多个URL以逗号分隔）")
    private String productImages;

    /** 商品描述 */
    @ApiModelProperty("商品描述")
    private String description;

    /** 积分价格 */
    @ApiModelProperty("积分价格")
    private Integer pointsPrice;

    /** 库存数量 */
    @ApiModelProperty("库存数量")
    private Integer stock;

    /** 总兑换数 */
    @ApiModelProperty("总兑换数")
    private Integer totalExchange;

    /** 状态（0-下架 1-上架） */
    @ApiModelProperty("状态（0-下架 1-上架）")
    private String status;

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

    /** 分类名称（关联查询） */
    @ApiModelProperty("分类名称")
    @TableField(exist = false)
    private String categoryName;
}

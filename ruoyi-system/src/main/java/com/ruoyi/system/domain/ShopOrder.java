package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 兑换订单表 shop_order
 *
 * @author ruoyi
 */
@Data
@TableName("shop_order")
public class ShopOrder implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    @ApiModelProperty("订单编号")
    @Excel(name = "订单编号")
    private String orderNo;

    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 商品ID */
    @ApiModelProperty("商品ID")
    private Long productId;

    /** 商品名称（快照） */
    @ApiModelProperty("商品名称（快照）")
    @Excel(name = "商品名称")
    private String productName;

    /** 商品图片（快照） */
    @ApiModelProperty("商品图片（快照）")
    private String productImage;

    /** 兑换积分 */
    @ApiModelProperty("兑换积分")
    @Excel(name = "兑换积分", cellType = Excel.ColumnType.NUMERIC)
    private Integer pointsPrice;

    /** 兑换数量 */
    @ApiModelProperty("兑换数量")
    @Excel(name = "兑换数量", cellType = Excel.ColumnType.NUMERIC)
    private Integer quantity;

    /** 总积分 */
    @ApiModelProperty("总积分")
    @Excel(name = "总积分", cellType = Excel.ColumnType.NUMERIC)
    private Integer totalPoints;

    /** 收货地址ID */
    @ApiModelProperty("收货地址ID")
    private Long addressId;

    /** 订单状态（0-待发货 1-已发货 2-已完成 3-已取消） */
    @ApiModelProperty("订单状态（0-待发货 1-已发货 2-已完成 3-已取消）")
    @Excel(name = "订单状态", readConverterExp = "0=待发货,1=已发货,2=已完成,3=已取消")
    private String status;

    /** 快递单号 */
    @ApiModelProperty("快递单号")
    @Excel(name = "快递单号")
    private String expressNo;

    /** 快递公司 */
    @ApiModelProperty("快递公司")
    @Excel(name = "快递公司")
    private String expressCompany;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @Excel(name = "下单时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 下单用户名（关联查询） */
    @ApiModelProperty("下单用户名")
    @Excel(name = "下单用户")
    @TableField(exist = false)
    private String userName;
}

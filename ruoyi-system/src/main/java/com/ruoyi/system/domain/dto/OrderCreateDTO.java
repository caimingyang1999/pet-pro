package com.ruoyi.system.domain.dto;

import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建订单参数
 *
 * @author ruoyi
 */
@Data
@ApiModel("创建订单参数")
public class OrderCreateDTO
{
    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 商品ID */
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty("商品ID")
    private Long productId;

    /** 兑换数量 */
    @NotNull(message = "兑换数量不能为空")
    @ApiModelProperty("兑换数量")
    private Integer quantity;

    /** 收货地址ID */
    @NotNull(message = "收货地址ID不能为空")
    @ApiModelProperty("收货地址ID")
    private Long addressId;
}

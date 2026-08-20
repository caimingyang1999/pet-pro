package com.ruoyi.system.domain.vo;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.system.domain.UserAddress;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单详情返回对象（含关联用户和收货地址信息）
 *
 * @author ruoyi
 */
@Data
@ApiModel("订单详情返回对象")
public class OrderDetailVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单ID")
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("商品名称（快照）")
    private String productName;

    @ApiModelProperty("商品图片（快照）")
    private String productImage;

    @ApiModelProperty("兑换积分")
    private Integer pointsPrice;

    @ApiModelProperty("兑换数量")
    private Integer quantity;

    @ApiModelProperty("总积分")
    private Integer totalPoints;

    @ApiModelProperty("收货地址ID")
    private Long addressId;

    @ApiModelProperty("订单状态（0-待发货 1-已发货 2-已完成 3-已取消）")
    private String status;

    @ApiModelProperty("快递单号")
    private String expressNo;

    @ApiModelProperty("快递公司")
    private String expressCompany;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @ApiModelProperty("关联用户信息")
    private UserInfo user;

    @ApiModelProperty("关联收货地址信息")
    private UserAddress address;

    /**
     * 下单用户简要信息
     */
    @Data
    @ApiModel("下单用户简要信息")
    public static class UserInfo implements Serializable
    {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("用户ID")
        private Long userId;

        @ApiModelProperty("昵称")
        private String nickName;

        @ApiModelProperty("头像")
        private String avatar;

        @ApiModelProperty("手机号")
        private String phonenumber;
    }
}

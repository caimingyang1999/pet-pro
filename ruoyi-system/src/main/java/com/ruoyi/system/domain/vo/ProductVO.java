package com.ruoyi.system.domain.vo;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品返回对象
 *
 * @author ruoyi
 */
@Data
@ApiModel("商品返回对象")
public class ProductVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("商品ID")
    private Long id;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品图片")
    private String productImages;

    @ApiModelProperty("商品描述")
    private String description;

    @ApiModelProperty("积分价格")
    private Integer pointsPrice;

    @ApiModelProperty("库存数量")
    private Integer stock;

    @ApiModelProperty("总兑换数")
    private Integer totalExchange;

    @ApiModelProperty("状态（0-下架 1-上架）")
    private String status;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}

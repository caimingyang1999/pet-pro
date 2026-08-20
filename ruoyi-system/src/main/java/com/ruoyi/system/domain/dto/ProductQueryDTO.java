package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品查询参数
 *
 * @author ruoyi
 */
@Data
@ApiModel("商品查询参数")
public class ProductQueryDTO
{
    /** 分类ID */
    @ApiModelProperty("分类ID")
    private Long categoryId;

    /** 关键词 */
    @ApiModelProperty("关键词")
    private String keyword;

    /** 当前页码 */
    @ApiModelProperty("当前页码")
    private Integer pageNum = 1;

    /** 每页条数 */
    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;
}

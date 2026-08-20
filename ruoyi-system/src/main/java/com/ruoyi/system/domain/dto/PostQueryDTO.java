package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 动态查询参数
 *
 * @author ruoyi
 */
@Data
@ApiModel("动态查询参数")
public class PostQueryDTO
{
    /** 关键词 */
    @ApiModelProperty("关键词")
    private String keyword;

    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 当前页码 */
    @ApiModelProperty("当前页码")
    private Integer pageNum = 1;

    /** 每页条数 */
    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;
}

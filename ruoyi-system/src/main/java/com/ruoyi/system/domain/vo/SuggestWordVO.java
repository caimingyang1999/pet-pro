package com.ruoyi.system.domain.vo;

import java.io.Serializable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * AI搜索推荐词 展示对象
 *
 * @author ruoyi
 */
@Data
public class SuggestWordVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    private Long id;

    /** 提示词内容 */
    @ApiModelProperty("提示词内容")
    private String word;

    /** 宠物类型 */
    @ApiModelProperty("宠物类型")
    private String petType;

    /** 分类 */
    @ApiModelProperty("分类")
    private String category;

    /** 推荐得分（用于排序） */
    @ApiModelProperty("推荐得分（用于排序）")
    private Integer score;
}

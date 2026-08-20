package com.ruoyi.system.domain.dto;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * AI搜索推荐词 新增/编辑/查询请求参数
 *
 * @author ruoyi
 */
@Data
@ApiModel("AI搜索推荐词请求参数")
public class AiSuggestWordDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID（编辑时必填） */
    @ApiModelProperty("主键ID（编辑时必填）")
    private Long id;

    /** 提示词内容 */
    @ApiModelProperty("提示词内容")
    @NotBlank(message = "提示词内容不能为空")
    private String word;

    /** 宠物类型（general-通用/cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼/other-其他） */
    @ApiModelProperty("宠物类型（general-通用/cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼/other-其他）")
    @NotBlank(message = "宠物类型不能为空")
    private String petType;

    /** 分类（general-通用/care-护理/diet-饮食/medical-医疗/behavior-行为/training-训练） */
    @ApiModelProperty("分类（general-通用/care-护理/diet-饮食/medical-医疗/behavior-行为/training-训练）")
    private String category;

    /** 基础权重（数字越大越靠前） */
    @ApiModelProperty("基础权重（数字越大越靠前）")
    private Integer weight;

    /** 状态（0-停用 1-启用） */
    @ApiModelProperty("状态（0-停用 1-启用）")
    private String status;

    /** 当前页码（后台分页查询用） */
    @ApiModelProperty("当前页码")
    private Integer pageNum = 1;

    /** 每页条数（后台分页查询用） */
    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;
}

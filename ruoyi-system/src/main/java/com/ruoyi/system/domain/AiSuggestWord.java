package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * AI搜索推荐词表 ai_suggest_word
 *
 * @author ruoyi
 */
@Data
@TableName("ai_suggest_word")
public class AiSuggestWord implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提示词内容 */
    @ApiModelProperty("提示词内容")
    private String word;

    /** 宠物类型（general-通用/cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼/other-其他） */
    @ApiModelProperty("宠物类型（general-通用/cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼/other-其他）")
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

    /** 删除标志（0-存在 1-删除） */
    @ApiModelProperty("删除标志（0-存在 1-删除）")
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
}

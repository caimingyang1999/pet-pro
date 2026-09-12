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
 * 养宠知识文章表 pet_article
 *
 * 由平台（管理员）发布，小程序端只读浏览，不提供评论、点赞、分享等互动能力。
 *
 * @author ruoyi
 */
@Data
@TableName("pet_article")
public class PetArticle implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章标题 */
    @ApiModelProperty("文章标题")
    private String title;

    /** 摘要（列表页展示） */
    @ApiModelProperty("摘要")
    private String summary;

    /** 封面图地址 */
    @ApiModelProperty("封面图地址")
    private String coverImage;

    /** 正文（富文本 HTML） */
    @ApiModelProperty("正文（富文本HTML）")
    private String content;

    /** 分类（如：喂养/健康/训练/洗护） */
    @ApiModelProperty("分类")
    private String category;

    /**
     * 适用宠物
     *
     * 单值，取值与 ai_suggest_word.pet_type 统一：
     * general-通用 / cat-猫 / dog-狗 / rabbit-兔子 / bird-鸟 / fish-鱼 / other-其他。
     * general（或 NULL）表示所有宠物都适用，在兴趣推荐里作为兜底档排在命中的类型之后。
     */
    @ApiModelProperty("适用宠物（general-通用/cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼/other-其他）")
    private String petType;

    /** 标签（逗号分隔） */
    @ApiModelProperty("标签")
    private String tags;

    /** 来源 */
    @ApiModelProperty("来源")
    private String source;

    /** 浏览量 */
    @ApiModelProperty("浏览量")
    private Integer viewCount;

    /** 排序号（数字越小越靠前） */
    @ApiModelProperty("排序号")
    private Integer sortOrder;

    /** 状态（0-草稿 1-已发布） */
    @ApiModelProperty("状态（0-草稿 1-已发布）")
    private String status;

    /** 创建人 */
    @ApiModelProperty("创建人")
    private String createBy;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新人 */
    @ApiModelProperty("更新人")
    private String updateBy;

    /** 更新时间 */
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 删除标志（0-存在 1-删除） */
    @ApiModelProperty("删除标志")
    @TableLogic
    private String delFlag;
}

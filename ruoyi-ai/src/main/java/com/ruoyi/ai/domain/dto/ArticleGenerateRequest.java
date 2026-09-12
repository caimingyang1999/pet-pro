package com.ruoyi.ai.domain.dto;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * AI 文章生成请求参数
 *
 * @author ruoyi
 */
@ApiModel("AI 文章生成请求")
public class ArticleGenerateRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 适用宠物类型（general/cat/dog/rabbit/bird/fish，可空） */
    @ApiModelProperty("适用宠物类型（general/cat/dog/rabbit/bird/fish，可空）")
    private String petType;

    /** 主题分类（喂养/健康/训练/洗护/疾病，可空由模型挑选） */
    @ApiModelProperty("主题分类（喂养/健康/训练/洗护/疾病，可空）")
    private String category;

    /** 指定选题（可空，由模型自拟） */
    @ApiModelProperty("指定选题（可空，由模型自拟）")
    private String topic;

    /** 是否生成配图（默认 true） */
    @ApiModelProperty("是否生成配图（默认 true）")
    private Boolean withImage = true;

    /** 正文内嵌配图数量（默认 1，最大 3） */
    @ApiModelProperty("正文内嵌配图数量（默认 1，最大 3）")
    private Integer inlineCount;

    public String getPetType()
    {
        return petType;
    }

    public void setPetType(String petType)
    {
        this.petType = petType;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getTopic()
    {
        return topic;
    }

    public void setTopic(String topic)
    {
        this.topic = topic;
    }

    public Boolean getWithImage()
    {
        return withImage;
    }

    public void setWithImage(Boolean withImage)
    {
        this.withImage = withImage;
    }

    public Integer getInlineCount()
    {
        return inlineCount;
    }

    public void setInlineCount(Integer inlineCount)
    {
        this.inlineCount = inlineCount;
    }
}

package com.ruoyi.ai.domain.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;

/**
 * Node.js 文章生成服务返回结果
 *
 * 外层：{ success, code, message, data }
 * data：{ title, summary, category, petType, tags, source, content, cover, images, warnings }
 * cover：{ base64, ext, provider } 或 null
 * images：[{ token, base64, ext, provider }]
 *
 * @author ruoyi
 */
@ApiModel("AI 文章生成结果")
public class ArticleGenerateResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Boolean success;

    private Integer code;

    private String message;

    private Data data;

    public Boolean getSuccess()
    {
        return success;
    }

    public void setSuccess(Boolean success)
    {
        this.success = success;
    }

    public Integer getCode()
    {
        return code;
    }

    public void setCode(Integer code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Data getData()
    {
        return data;
    }

    public void setData(Data data)
    {
        this.data = data;
    }

    public static class Data implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private String title;
        private String summary;
        private String category;
        private String petType;
        private String tags;
        private String source;
        private String content;
        private CoverImage cover;
        private List<InlineImage> images;
        private List<String> warnings;

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public String getSummary()
        {
            return summary;
        }

        public void setSummary(String summary)
        {
            this.summary = summary;
        }

        public String getCategory()
        {
            return category;
        }

        public void setCategory(String category)
        {
            this.category = category;
        }

        public String getPetType()
        {
            return petType;
        }

        public void setPetType(String petType)
        {
            this.petType = petType;
        }

        public String getTags()
        {
            return tags;
        }

        public void setTags(String tags)
        {
            this.tags = tags;
        }

        public String getSource()
        {
            return source;
        }

        public void setSource(String source)
        {
            this.source = source;
        }

        public String getContent()
        {
            return content;
        }

        public void setContent(String content)
        {
            this.content = content;
        }

        public CoverImage getCover()
        {
            return cover;
        }

        public void setCover(CoverImage cover)
        {
            this.cover = cover;
        }

        public List<InlineImage> getImages()
        {
            return images;
        }

        public void setImages(List<InlineImage> images)
        {
            this.images = images;
        }

        public List<String> getWarnings()
        {
            return warnings;
        }

        public void setWarnings(List<String> warnings)
        {
            this.warnings = warnings;
        }
    }

    public static class CoverImage implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private String base64;
        private String ext;
        private String provider;

        public String getBase64()
        {
            return base64;
        }

        public void setBase64(String base64)
        {
            this.base64 = base64;
        }

        public String getExt()
        {
            return ext;
        }

        public void setExt(String ext)
        {
            this.ext = ext;
        }

        public String getProvider()
        {
            return provider;
        }

        public void setProvider(String provider)
        {
            this.provider = provider;
        }
    }

    public static class InlineImage implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private String token;
        private String base64;
        private String ext;
        private String provider;

        public String getToken()
        {
            return token;
        }

        public void setToken(String token)
        {
            this.token = token;
        }

        public String getBase64()
        {
            return base64;
        }

        public void setBase64(String base64)
        {
            this.base64 = base64;
        }

        public String getExt()
        {
            return ext;
        }

        public void setExt(String ext)
        {
            this.ext = ext;
        }

        public String getProvider()
        {
            return provider;
        }

        public void setProvider(String provider)
        {
            this.provider = provider;
        }
    }
}

package com.ruoyi.ai.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * AI对话消息 ai_chat_message
 *
 * @author ruoyi
 */
@Data
@TableName("ai_chat_message")
@ApiModel("AI对话消息")
public class AiChatMessage implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    @ApiModelProperty("会话ID")
    private Long sessionId;

    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 消息角色（user-用户消息 assistant-AI回复 system-系统消息） */
    @ApiModelProperty("消息角色")
    private String role;

    /** 消息内容 */
    @ApiModelProperty("消息内容")
    private String content;

    /** 内容类型（text-文本 image-图片） */
    @ApiModelProperty("内容类型")
    private String contentType;

    /** 消耗Token数 */
    @ApiModelProperty("消耗Token数")
    private Integer tokenUsed;

    /** 使用的AI模型名称 */
    @ApiModelProperty("使用的AI模型名称")
    private String model;

    /** 消息状态（1-成功 0-失败 2-生成中） */
    @ApiModelProperty("消息状态")
    private String status;

    /** 错误信息（生成失败时记录） */
    @ApiModelProperty("错误信息")
    private String errorMsg;

    /** 删除标志（0-存在 1-删除） */
    @ApiModelProperty("删除标志")
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

    /** 会话标题（关联查询，非数据库字段） */
    @ApiModelProperty("会话标题")
    @TableField(exist = false)
    private String sessionTitle;
}

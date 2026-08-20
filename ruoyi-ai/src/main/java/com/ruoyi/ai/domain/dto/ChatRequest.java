package com.ruoyi.ai.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * AI对话请求参数
 *
 * @author ruoyi
 */
@Data
@ApiModel("AI对话请求参数")
public class ChatRequest
{
    /** 用户输入内容 */
    @ApiModelProperty(value = "用户输入内容", required = true)
    @NotBlank(message = "请提供对话消息")
    private String message;

    /** 会话ID，用于多轮记忆隔离，不传时服务端自动生成 */
    @ApiModelProperty("会话ID（不传时服务端自动生成）")
    private String sessionId;
}

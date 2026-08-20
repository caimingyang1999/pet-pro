package com.ruoyi.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * AI对话配置（对接Node.js流式AI服务）
 *
 * @author ruoyi
 */
@Configuration
public class AiChatConfig
{
    /** Node.js AI服务基础地址 */
    @Value("${ai.chat.base-url}")
    private String baseUrl;

    /** 对话接口路径 */
    @Value("${ai.chat.chat-path}")
    private String chatPath;

    /** 清空历史接口路径 */
    @Value("${ai.chat.history-path}")
    private String historyPath;

    /**
     * 构建调用Node.js AI服务的WebClient
     */
    @Bean
    public WebClient aiChatWebClient()
    {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();
    }

    public String getChatPath()
    {
        return chatPath;
    }

    public String getHistoryPath()
    {
        return historyPath;
    }
}

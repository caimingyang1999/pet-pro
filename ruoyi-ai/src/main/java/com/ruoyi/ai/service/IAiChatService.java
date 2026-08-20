package com.ruoyi.ai.service;

import reactor.core.publisher.Flux;

/**
 * AI对话编排 服务层（对接Node.js流式AI服务，保留对话记录与记忆）
 *
 * @author ruoyi
 */
public interface IAiChatService
{
    /**
     * 流式对话：调用Node.js AI服务，同时持久化对话记录
     *
     * @param message   用户输入内容
     * @param sessionId 会话ID（不传时服务端自动生成）
     * @param userId    用户ID
     * @return SSE数据流，每个元素为一段JSON载荷
     */
    public Flux<String> chat(String message, String sessionId, Long userId);

    /**
     * 清空指定会话的对话记忆（同时清理Node.js内存历史与数据库记录）
     *
     * @param sessionId 会话ID
     */
    public void clearHistory(String sessionId);
}

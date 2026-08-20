package com.ruoyi.ai.service.impl;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.ruoyi.ai.config.AiChatConfig;
import com.ruoyi.ai.domain.AiChatMessage;
import com.ruoyi.ai.domain.AiChatSession;
import com.ruoyi.ai.mapper.AiChatMessageMapper;
import com.ruoyi.ai.mapper.AiChatSessionMapper;
import com.ruoyi.ai.service.IAiChatService;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * AI对话编排 服务实现
 * 对接Node.js流式AI服务，同时将对话记录持久化到数据库，保留多轮记忆。
 *
 * @author ruoyi
 */
@Service
public class AiChatServiceImpl implements IAiChatService
{
    @Resource
    private AiChatSessionMapper aiChatSessionMapper;

    @Resource
    private AiChatMessageMapper aiChatMessageMapper;

    @Resource
    private WebClient aiChatWebClient;

    @Resource
    private AiChatConfig aiChatConfig;

    /**
     * 流式对话：调用Node.js AI服务，同时持久化对话记录
     *
     * @param message   用户输入内容
     * @param sessionId 会话ID（不传时服务端自动生成）
     * @param userId    用户ID
     * @return SSE数据流，每个元素为一段JSON载荷
     */
    @Override
    public Flux<String> chat(String message, String sessionId, Long userId)
    {
        // 1. 创建或获取会话记录
        AiChatSession session = getOrCreateSession(sessionId, userId, message);
        // 2. 保存用户消息
        saveUserMessage(session.getId(), userId, message);
        // 3. 调用Node.js，流式返回，同时累积AI回复内容
        String nodeSessionId = "pet-" + session.getId();
        StringBuilder replyBuffer = new StringBuilder();
        String[] errorHolder = {null};

        return callNodeChat(message, nodeSessionId)
                // 累积AI回复内容，并捕获错误信息
                .doOnNext(data -> accumulateReply(data, replyBuffer, errorHolder))
                // 流结束后持久化AI回复
                .doOnComplete(() -> saveAiReply(session.getId(), userId, replyBuffer.toString(), errorHolder[0]))
                // 流异常时记录错误
                .doOnError(e -> saveAiReply(session.getId(), userId, "", e.getMessage()));
    }

    /**
     * 清空指定会话的对话记忆（同时清理Node.js内存历史与数据库记录）
     *
     * @param sessionId 会话ID
     */
    @Override
    public void clearHistory(String sessionId)
    {
        if (!StringUtils.hasText(sessionId))
        {
            throw new ServiceException("会话ID不能为空");
        }
        Long dbSessionId = parseDbSessionId(sessionId);
        if (dbSessionId == null)
        {
            throw new ServiceException("无效的会话ID");
        }
        // 1. 调用Node.js清空内存历史
        clearNodeHistory(sessionId);
        // 2. 逻辑删除数据库中的消息记录
        aiChatMessageMapper.update(null,
                new LambdaUpdateWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, dbSessionId)
                        .set(AiChatMessage::getDelFlag, "1"));
        // 3. 逻辑删除会话记录
        aiChatSessionMapper.update(null,
                new LambdaUpdateWrapper<AiChatSession>()
                        .eq(AiChatSession::getId, dbSessionId)
                        .set(AiChatSession::getStatus, "0"));
    }

    /**
     * 创建或获取会话记录
     * sessionId格式为 pet-{数据库会话ID}，由服务端生成并回传给前端
     */
    private AiChatSession getOrCreateSession(String sessionId, Long userId, String message)
    {
        Long dbSessionId = parseDbSessionId(sessionId);
        // 尝试查找已存在的会话
        if (dbSessionId != null)
        {
            AiChatSession session = aiChatSessionMapper.selectById(dbSessionId);
            if (session != null && session.getUserId().equals(userId) && "1".equals(session.getStatus()))
            {
                return session;
            }
        }
        // 创建新会话
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setSessionTitle(generateTitle(message));
        session.setSessionType("general");
        session.setModel("deepseek-chat");
        session.setMessageCount(0);
        session.setStatus("1");
        aiChatSessionMapper.insert(session);
        return session;
    }

    /**
     * 从会话ID字符串中解析数据库会话主键
     * sessionId格式为 pet-{id}
     */
    private Long parseDbSessionId(String sessionId)
    {
        if (!StringUtils.hasText(sessionId) || !sessionId.startsWith("pet-"))
        {
            return null;
        }
        try
        {
            return Long.parseLong(sessionId.substring(4));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * 根据用户首条消息生成会话标题
     */
    private String generateTitle(String message)
    {
        if (!StringUtils.hasText(message))
        {
            return "新对话";
        }
        return message.length() > 20 ? message.substring(0, 20) + "..." : message;
    }

    /**
     * 保存用户消息
     */
    private void saveUserMessage(Long sessionId, Long userId, String content)
    {
        AiChatMessage msg = new AiChatMessage();
        msg.setSessionId(sessionId);
        msg.setUserId(userId);
        msg.setRole("user");
        msg.setContent(content);
        msg.setContentType("text");
        msg.setStatus("1");
        aiChatMessageMapper.insert(msg);
    }

    /**
     * 保存AI回复消息，并更新会话消息计数
     *
     * @param sessionId 数据库会话ID
     * @param userId    用户ID
     * @param content   AI回复完整内容
     * @param errorMsg  错误信息（非空表示生成失败）
     */
    private void saveAiReply(Long sessionId, Long userId, String content, String errorMsg)
    {
        AiChatMessage msg = new AiChatMessage();
        msg.setSessionId(sessionId);
        msg.setUserId(userId);
        msg.setRole("assistant");
        msg.setContent(content != null ? content : "");
        msg.setContentType("text");
        msg.setModel("deepseek-chat");
        // 有错误或回复内容为空时标记为失败
        msg.setStatus(StringUtils.hasText(errorMsg) || !StringUtils.hasText(content) ? "0" : "1");
        msg.setErrorMsg(errorMsg);
        aiChatMessageMapper.insert(msg);
        // 更新会话消息总数（用户消息+AI回复共2条）
        aiChatSessionMapper.update(null,
                new LambdaUpdateWrapper<AiChatSession>()
                        .eq(AiChatSession::getId, sessionId)
                        .setSql("message_count = message_count + 2"));
    }

    /**
     * 累积AI回复内容，并捕获错误信息
     *
     * @param data        SSE数据载荷（JSON字符串）
     * @param replyBuffer 回复内容缓冲区
     * @param errorHolder 错误信息持有者（数组长度1）
     */
    private void accumulateReply(String data, StringBuilder replyBuffer, String[] errorHolder)
    {
        try
        {
            JSONObject obj = JSON.parseObject(data);
            String type = obj.getString("type");
            if ("chunk".equals(type))
            {
                String content = obj.getString("content");
                if (content != null)
                {
                    replyBuffer.append(content);
                }
            }
            else if ("error".equals(type))
            {
                errorHolder[0] = obj.getString("message");
            }
        }
        catch (Exception ignored)
        {
        }
    }

    /**
     * 调用Node.js AI服务，获取SSE流式响应
     *
     * @param message      用户输入内容
     * @param nodeSessionId 会话ID（pet-{id}格式）
     * @return SSE数据流
     */
    private Flux<String> callNodeChat(String message, String nodeSessionId)
    {
        Map<String, Object> body = new HashMap<>(2);
        body.put("message", message);
        body.put("sessionId", nodeSessionId);

        return aiChatWebClient.post()
                .uri(aiChatConfig.getChatPath())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(e -> Flux.just("{\"type\":\"error\",\"message\":\"AI服务调用失败：" + e.getMessage() + "\",\"sessionId\":\"" + nodeSessionId + "\"}"));
    }

    /**
     * 调用Node.js清空指定会话的内存历史
     *
     * @param sessionId 会话ID
     */
    private void clearNodeHistory(String sessionId)
    {
        try
        {
            aiChatWebClient.delete()
                    .uri(aiChatConfig.getHistoryPath() + "/" + sessionId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe();
        }
        catch (Exception ignored)
        {
            // Node.js清空历史失败不影响数据库记录清理
        }
    }
}

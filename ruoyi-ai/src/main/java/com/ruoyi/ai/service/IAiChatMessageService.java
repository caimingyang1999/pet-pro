package com.ruoyi.ai.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.ai.domain.AiChatMessage;

/**
 * AI对话消息 服务层
 *
 * @author ruoyi
 */
public interface IAiChatMessageService extends IService<AiChatMessage>
{
    /**
     * 查询指定会话的消息列表（按时间正序）
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    public List<AiChatMessage> getMessageListBySessionId(Long sessionId);
}

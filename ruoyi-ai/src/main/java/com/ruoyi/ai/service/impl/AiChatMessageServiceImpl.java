package com.ruoyi.ai.service.impl;

import java.util.List;
import javax.annotation.Resource;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.ai.domain.AiChatMessage;
import com.ruoyi.ai.mapper.AiChatMessageMapper;
import com.ruoyi.ai.service.IAiChatMessageService;
import org.springframework.stereotype.Service;

/**
 * AI对话消息 服务实现
 *
 * @author ruoyi
 */
@Service
public class AiChatMessageServiceImpl extends ServiceImpl<AiChatMessageMapper, AiChatMessage> implements IAiChatMessageService
{
    @Resource
    private AiChatMessageMapper aiChatMessageMapper;

    /**
     * 查询指定会话的消息列表（按时间正序）
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @Override
    public List<AiChatMessage> getMessageListBySessionId(Long sessionId)
    {
        return aiChatMessageMapper.selectMessageListBySessionId(sessionId);
    }
}

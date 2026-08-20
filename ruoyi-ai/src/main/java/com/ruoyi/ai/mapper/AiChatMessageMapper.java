package com.ruoyi.ai.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.ai.domain.AiChatMessage;
import org.apache.ibatis.annotations.Param;

/**
 * AI对话消息 数据层
 *
 * @author ruoyi
 */
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage>
{
    /**
     * 查询指定会话的消息列表（按时间正序）
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    public List<AiChatMessage> selectMessageListBySessionId(@Param("sessionId") Long sessionId);
}

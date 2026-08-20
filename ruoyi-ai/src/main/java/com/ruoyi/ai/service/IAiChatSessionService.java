package com.ruoyi.ai.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.ai.domain.AiChatSession;

/**
 * AI对话会话 服务层
 *
 * @author ruoyi
 */
public interface IAiChatSessionService extends IService<AiChatSession>
{
    /**
     * 查询用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    public List<AiChatSession> getSessionList(Long userId);
}

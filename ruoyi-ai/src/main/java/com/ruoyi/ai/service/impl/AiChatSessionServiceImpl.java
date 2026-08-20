package com.ruoyi.ai.service.impl;

import java.util.List;
import javax.annotation.Resource;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.ai.domain.AiChatSession;
import com.ruoyi.ai.mapper.AiChatSessionMapper;
import com.ruoyi.ai.service.IAiChatSessionService;
import org.springframework.stereotype.Service;

/**
 * AI对话会话 服务实现
 *
 * @author ruoyi
 */
@Service
public class AiChatSessionServiceImpl extends ServiceImpl<AiChatSessionMapper, AiChatSession> implements IAiChatSessionService
{
    @Resource
    private AiChatSessionMapper aiChatSessionMapper;

    /**
     * 查询用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @Override
    public List<AiChatSession> getSessionList(Long userId)
    {
        return aiChatSessionMapper.selectSessionList(userId);
    }
}

package com.ruoyi.ai.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.ai.domain.AiChatSession;
import org.apache.ibatis.annotations.Param;

/**
 * AI对话会话 数据层
 *
 * @author ruoyi
 */
public interface AiChatSessionMapper extends BaseMapper<AiChatSession>
{
    /**
     * 查询用户的会话列表（关联用户昵称）
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    public List<AiChatSession> selectSessionList(@Param("userId") Long userId);
}

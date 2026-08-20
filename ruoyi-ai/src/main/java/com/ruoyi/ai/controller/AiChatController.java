package com.ruoyi.ai.controller;

import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.ruoyi.ai.domain.AiChatMessage;
import com.ruoyi.ai.domain.AiChatSession;
import com.ruoyi.ai.domain.dto.ChatRequest;
import com.ruoyi.ai.service.IAiChatMessageService;
import com.ruoyi.ai.service.IAiChatService;
import com.ruoyi.ai.service.IAiChatSessionService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * AI对话 控制器
 * 作为中间代理对接Node.js流式AI服务，同时持久化对话记录与记忆。
 *
 * @author ruoyi
 */
@Api(tags = "AI对话")
@RestController
@RequestMapping("/api/v1/ai")
public class AiChatController extends BaseController
{
    @Resource
    private IAiChatService aiChatService;

    @Resource
    private IAiChatSessionService aiChatSessionService;

    @Resource
    private IAiChatMessageService aiChatMessageService;

    /**
     * AI流式对话（SSE）
     * 调用Node.js AI服务，流式返回AI回答，同时保存对话记录到数据库。
     *
     * @param req 对话请求（message-用户输入，sessionId-会话ID）
     * @return SSE流
     */
    @ApiOperation("AI流式对话（SSE）")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Validated @RequestBody ChatRequest req)
    {
        // 创建不超时的SSE发射器
        SseEmitter emitter = new SseEmitter(0L);
        Long userId = getUserId();

        aiChatService.chat(req.getMessage(), req.getSessionId(), userId)
                .doOnNext(data -> {
                    try
                    {
                        // 透传Node.js返回的SSE数据给前端
                        emitter.send(SseEmitter.event().data(data));
                    }
                    catch (IOException e)
                    {
                        emitter.completeWithError(e);
                    }
                })
                .doOnComplete(emitter::complete)
                .doOnError(emitter::completeWithError)
                .subscribe();

        return emitter;
    }

    /**
     * 清空指定会话的对话记忆
     * 同时清理Node.js内存历史与数据库记录。
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @ApiOperation("清空对话记忆")
    @PreAuthorize("@ss.isAuthenticated()")
    @DeleteMapping("/chat/history/{sessionId}")
    public AjaxResult clearHistory(
            @ApiParam(name = "sessionId", value = "会话ID", required = true)
            @PathVariable String sessionId)
    {
        aiChatService.clearHistory(sessionId);
        return AjaxResult.success("对话记忆已清空");
    }

    /**
     * 获取当前用户的会话列表
     *
     * @return 会话列表
     */
    @ApiOperation("获取会话列表")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/sessions")
    public AjaxResult listSessions()
    {
        Long userId = getUserId();
        List<AiChatSession> list = aiChatSessionService.getSessionList(userId);
        return AjaxResult.success(list);
    }

    /**
     * 获取指定会话的消息记录（对话历史）
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @ApiOperation("获取会话消息记录")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/chat/history/{sessionId}")
    public AjaxResult getHistory(
            @ApiParam(name = "sessionId", value = "会话ID", required = true)
            @PathVariable String sessionId)
    {
        Long dbSessionId = parseDbSessionId(sessionId);
        if (dbSessionId == null)
        {
            throw new ServiceException("无效的会话ID");
        }
        List<AiChatMessage> list = aiChatMessageService.getMessageListBySessionId(dbSessionId);
        return AjaxResult.success(list);
    }

    /**
     * 从会话ID字符串中解析数据库会话主键
     * sessionId格式为 pet-{id}
     */
    private Long parseDbSessionId(String sessionId)
    {
        if (sessionId == null || !sessionId.startsWith("pet-"))
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
}

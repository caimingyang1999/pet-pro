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
import com.ruoyi.system.service.ISysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 养宠顾问 控制器
 * 作为中间代理对接流式问答服务，同时持久化问答记录与记忆。
 *
 * @author ruoyi
 */
@Api(tags = "养宠顾问")
@RestController
@RequestMapping("/api/v1/adviser")
public class AiChatController extends BaseController
{
    /** 养宠顾问功能开关参数键（sys_config，true开启/false关闭） */
    private static final String CONFIG_KEY_ADVISER_ENABLED = "feature.adviser.enabled";

    @Resource
    private IAiChatService aiChatService;

    @Resource
    private IAiChatSessionService aiChatSessionService;

    @Resource
    private IAiChatMessageService aiChatMessageService;

    @Resource
    private ISysConfigService configService;

    /**
     * 养宠顾问功能是否启用（参数未配置时默认开启）
     */
    private boolean isAdviserEnabled()
    {
        return !"false".equalsIgnoreCase(configService.selectConfigByKey(CONFIG_KEY_ADVISER_ENABLED));
    }

    /**
     * 养宠顾问流式问答（SSE）
     * 调用流式问答服务，流式返回回答，同时保存问答记录到数据库。
     *
     * @param req 问答请求（message-用户输入，sessionId-会话ID）
     * @return SSE流
     */
    @ApiOperation("养宠顾问流式问答（SSE）")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Validated @RequestBody ChatRequest req)
    {
        // 创建不超时的SSE发射器
        SseEmitter emitter = new SseEmitter(0L);

        // 功能开关关闭：通过SSE错误事件通知前端，不调用问答服务
        if (!isAdviserEnabled())
        {
            try
            {
                emitter.send(SseEmitter.event().data("{\"type\":\"error\",\"message\":\"功能升级中，敬请期待\"}"));
            }
            catch (IOException e)
            {
                emitter.completeWithError(e);
            }
            emitter.complete();
            return emitter;
        }

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
     * 清空指定会话的问答记忆
     * 同时清理问答服务内存历史与数据库记录。
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @ApiOperation("清空问答记忆")
    @PreAuthorize("@ss.isAuthenticated()")
    @DeleteMapping("/chat/history/{sessionId}")
    public AjaxResult clearHistory(
            @ApiParam(name = "sessionId", value = "会话ID", required = true)
            @PathVariable String sessionId)
    {
        if (!isAdviserEnabled())
        {
            throw new ServiceException("功能升级中，敬请期待");
        }
        aiChatService.clearHistory(sessionId);
        return AjaxResult.success("问答记录已清空");
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
        if (!isAdviserEnabled())
        {
            throw new ServiceException("功能升级中，敬请期待");
        }
        Long userId = getUserId();
        List<AiChatSession> list = aiChatSessionService.getSessionList(userId);
        return AjaxResult.success(list);
    }

    /**
     * 获取指定会话的消息记录（问答历史）
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
        if (!isAdviserEnabled())
        {
            throw new ServiceException("功能升级中，敬请期待");
        }
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

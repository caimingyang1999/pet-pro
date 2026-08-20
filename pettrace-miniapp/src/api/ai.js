import { get, del } from './request.js';
import { aiChatStream } from '@/utils/sse.js';

/**
 * AI 流式对话（SSE）
 * @param {Object} options - { message, sessionId?, onChunk, onComplete, onError, onDone }
 * @returns {{ abort: Function }} 可调用 abort() 取消
 *
 * 事件回调：
 *   onChunk(payload)    增量内容，payload: { content, sessionId }
 *   onComplete(payload) 整轮完成，payload: { data:{success,reply,sessionId}, sessionId }
 *   onError(payload)    错误，payload: { message, sessionId }
 *   onDone()            流彻底结束（无论成功失败）
 */
export function chatStream(options) {
  return aiChatStream(options);
}

/**
 * 清空对话记忆（同时清理内存历史与数据库记录）
 * @param {string} sessionId - 会话 ID（格式 pet-{id}）
 */
export function clearChatHistory(sessionId) {
  return del(`/ai/chat/history/${sessionId}`);
}

/**
 * 获取会话列表（按创建时间倒序）
 * @returns data: [{ id, userId, sessionTitle, sessionType, model, messageCount, status, createTime, updateTime, nickName }]
 */
export function getSessionList() {
  return get('/ai/sessions');
}

/**
 * 获取会话消息记录（按时间正序，含用户消息与 AI 回复）
 * @param {string} sessionId - 会话 ID（格式 pet-{id}）
 * @returns data: [{ id, sessionId, userId, role, content, contentType, model?, status, createTime }]
 */
export function getChatHistory(sessionId) {
  return get(`/ai/chat/history/${sessionId}`);
}

/**
 * 获取AI搜索推荐词（根据用户宠物信息智能推荐）
 * @param {number} limit - 返回数量，默认8条
 * @returns data: [{ id, word, petType, category, score }]
 */
export function getSuggestWords(limit = 4) {
  return get('/ai/suggest-words', { limit });
}

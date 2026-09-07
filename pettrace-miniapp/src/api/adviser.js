import { get, del } from './request.js';
import { adviserChatStream } from '@/utils/sse.js';

/**
 * 养宠顾问流式问答（SSE）
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
  return adviserChatStream(options);
}

/**
 * 清空问答记忆（同时清理内存历史与服务端记录）
 * @param {string} sessionId - 会话 ID（格式 pet-{id}）
 */
export function clearChatHistory(sessionId) {
  return del(`/adviser/chat/history/${sessionId}`);
}

/**
 * 获取会话列表（按创建时间倒序）
 * @returns data: [{ id, userId, sessionTitle, sessionType, model, messageCount, status, createTime, updateTime, nickName }]
 */
export function getSessionList() {
  return get('/adviser/sessions');
}

/**
 * 获取会话消息记录（按时间正序，含用户提问与顾问回复）
 * @param {string} sessionId - 会话 ID（格式 pet-{id}）
 * @returns data: [{ id, sessionId, userId, role, content, contentType, model?, status, createTime }]
 */
export function getChatHistory(sessionId) {
  return get(`/adviser/chat/history/${sessionId}`);
}

/**
 * 获取推荐提问词（根据用户宠物信息匹配）
 * @param {number} limit - 返回数量，默认8条
 * @returns data: [{ id, word, petType, category, score }]
 */
export function getSuggestWords(limit = 4) {
  return get('/adviser/suggest-words', { limit });
}

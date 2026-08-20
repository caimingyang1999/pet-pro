import { BASE_URL } from '@/api/request.js';

/**
 * SSE 流式请求工具
 *
 * 兼容微信小程序（uni.request + enableChunked + onChunkReceived）
 * 与 H5（fetch + ReadableStream）两种环境。
 *
 * 后端 AI 对话 SSE 事件格式：
 *   data: {"type":"chunk","content":"xxx","sessionId":"pet-1"}      // 增量内容
 *   data: {"type":"complete","data":{success,reply,sessionId},...}  // 整轮结束
 *   data: {"type":"error","message":"xxx","sessionId":"pet-1"}      // 错误
 *   event: end
 *   data: {"done":true}                                             // 流终止
 *
 * 事件之间以空行 \n\n 分隔；单次 chunk 可能包含不完整事件，需跨次缓冲拼接。
 */

// #ifdef H5
/**
 * H5 端：基于 fetch + ReadableStream 读取 SSE 流
 */
function h5Stream({ url, token, data, callbacks }) {
  const controller = new AbortController();
  const decoder = new TextDecoder();
  let buffer = '';

  (async () => {
    try {
      const resp = await fetch(url, {
        method: 'POST',
        signal: controller.signal,
        headers: {
          'Content-Type': 'application/json',
          Accept: 'text/event-stream',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(data),
      });

      if (!resp.ok || !resp.body) {
        // 非流式错误（如 400 参数错误），尝试解析 JSON
        let errMsg = `HTTP ${resp.status}`;
        try {
          const errJson = await resp.json();
          errMsg = errJson.msg || errMsg;
        } catch (e) {}
        callbacks.onError({ message: errMsg });
        callbacks.onDone();
        return;
      }

      const reader = resp.body.getReader();
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        buffer = consumeEvents(buffer, callbacks);
      }
      // 处理残留
      if (buffer.trim()) consumeEvents(buffer + '\n\n', callbacks);
      callbacks.onDone();
    } catch (err) {
      if (err?.name === 'AbortError') {
        // 主动取消，不视为错误
        callbacks.onDone();
        return;
      }
      callbacks.onError({ message: err?.message || '网络请求失败' });
      callbacks.onDone();
    }
  })();

  return { abort: () => controller.abort() };
}
// #endif

// #ifndef H5
/**
 * 小程序端：基于 uni.request + enableChunked + onChunkReceived 读取 SSE 流
 */
function mpStream({ url, token, data, callbacks }) {
  const task = uni.request({
    url,
    method: 'POST',
    enableChunked: true,
    responseType: 'text',
    timeout: 60000,
    header: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      Authorization: `Bearer ${token}`,
    },
    data,
    success: (res) => {
      // 流式数据已通过 onChunkReceived 处理，这里处理非 2xx 的情况
      if (res.statusCode < 200 || res.statusCode >= 300) {
        const errData = res.data && typeof res.data === 'object' ? res.data : {};
        if (res.statusCode === 401) {
          // 复用 request.js 的登录过期处理
          try {
            uni.removeStorageSync('token');
            uni.removeStorageSync('userInfo');
          } catch (e) {}
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
        }
        callbacks.onError({ message: errData.msg || `HTTP ${res.statusCode}` });
      }
    },
    fail: (err) => {
      callbacks.onError({ message: err.errMsg || '网络请求失败' });
    },
    complete: () => {
      callbacks.onDone();
    },
  });

  let buffer = '';
  try {
    task.onChunkReceived((res) => {
      // 小程序返回的是 ArrayBuffer，需手动解码
      try {
        // 微信小程序：res.data 为 ArrayBuffer
        const chunkText = arrayBufferToString(res.data);
        buffer += chunkText;
        buffer = consumeEvents(buffer, callbacks);
      } catch (e) {
        console.error('[sse] onChunkReceived 解析失败', e);
      }
    });
  } catch (e) {
    // 部分平台不支持 onChunkReceived，降级为一次性请求
    console.warn('[sse] 当前环境不支持 onChunkReceived，无法接收流式数据', e);
  }

  return { abort: () => task.abort && task.abort() };
}

/**
 * ArrayBuffer → 字符串（优先用 TextDecoder，降级手动 UTF-8 解码）
 */
function arrayBufferToString(buf) {
  if (typeof TextDecoder !== 'undefined') {
    return new TextDecoder('utf-8').decode(buf);
  }
  // 降级方案：手动 UTF-8 解码
  const arr = new Uint8Array(buf);
  let str = '';
  let i = 0;
  while (i < arr.length) {
    const b1 = arr[i++];
    if (b1 < 0x80) {
      str += String.fromCharCode(b1);
    } else if (b1 < 0xe0) {
      const b2 = arr[i++];
      str += String.fromCharCode(((b1 & 0x1f) << 6) | (b2 & 0x3f));
    } else if (b1 < 0xf0) {
      const b2 = arr[i++];
      const b3 = arr[i++];
      str += String.fromCharCode(((b1 & 0x0f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f));
    } else {
      const b2 = arr[i++];
      const b3 = arr[i++];
      const b4 = arr[i++];
      const codePoint = ((b1 & 0x07) << 18) | ((b2 & 0x3f) << 12) | ((b3 & 0x3f) << 6) | (b4 & 0x3f);
      // 转为 UTF-16 代理对
      const offset = codePoint - 0x10000;
      str += String.fromCharCode(0xd800 + (offset >> 10), 0xdc00 + (offset & 0x3ff));
    }
  }
  return str;
}
// #endif

/**
 * 从缓冲区中消费已完整的 SSE 事件（以空行分隔）
 * 返回剩余不完整的内容
 */
function consumeEvents(buffer, callbacks) {
  // SSE 事件之间用 \n\n 分隔；兼容 \r\n\r\n
  const parts = buffer.split(/\n\n|\r\n\r\n/);
  // 最后一段可能不完整，保留到下次
  const remainder = parts.pop() || '';
  for (const part of parts) {
    if (!part.trim()) continue;
    parseEvent(part, callbacks);
  }
  return remainder;
}

/**
 * 解析单个 SSE 事件块
 */
function parseEvent(rawEvent, callbacks) {
  let eventType = 'message';
  let dataLines = [];

  for (const line of rawEvent.split(/\n|\r\n/)) {
    if (!line) continue;
    if (line.startsWith('event:')) {
      eventType = line.slice(6).trim();
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim());
    }
  }

  const dataStr = dataLines.join('\n');
  if (!dataStr) return;

  // 终止事件：event: end / data: {"done":true}
  if (eventType === 'end') {
    callbacks.onEnd && callbacks.onEnd();
    return;
  }

  // 尝试解析 JSON
  let payload;
  try {
    payload = JSON.parse(dataStr);
  } catch (e) {
    // 非 JSON 数据，作为纯文本 chunk 处理
    payload = { type: 'chunk', content: dataStr };
  }

  // 按 type 分发
  if (payload.type === 'chunk') {
    callbacks.onChunk(payload);
  } else if (payload.type === 'complete') {
    callbacks.onComplete(payload);
  } else if (payload.type === 'error') {
    callbacks.onError(payload);
  } else {
    // 兜底：未知事件透传
    callbacks.onChunk && callbacks.onChunk(payload);
  }
}

/**
 * 发起 AI 流式对话
 * @param {Object} options
 * @param {string} options.message - 用户本轮输入
 * @param {string} [options.sessionId] - 会话 ID，不传则服务端创建新会话
 * @param {Function} options.onChunk - 增量内容回调 (payload) => void，payload: { content, sessionId }
 * @param {Function} options.onComplete - 整轮完成回调 (payload) => void
 * @param {Function} options.onError - 错误回调 (payload) => void，payload: { message, sessionId }
 * @param {Function} options.onDone - 流彻底结束回调（无论成功失败）
 * @returns {{ abort: Function }} 可调用 abort() 取消请求
 */
export function aiChatStream({ message, sessionId, onChunk, onComplete, onError, onDone }) {
  const token = uni.getStorageSync('token') || '';
  const url = `${BASE_URL}/ai/chat`;
  const data = { message };
  if (sessionId) data.sessionId = sessionId;

  const callbacks = {
    onChunk: onChunk || (() => {}),
    onComplete: onComplete || (() => {}),
    onError: onError || (() => {}),
    onDone: onDone || (() => {}),
    onEnd: () => {},
  };

  let handle;
  // #ifdef H5
  handle = h5Stream({ url, token, data, callbacks });
  // #endif
  // #ifndef H5
  handle = mpStream({ url, token, data, callbacks });
  // #endif
  return handle;
}

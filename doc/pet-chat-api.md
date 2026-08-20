# 宠物对话接口文档

> 该接口基于 Server-Sent Events (SSE) 实现流式返回，并支持基于 `sessionId` 的多轮对话记忆。适用于 Spring Boot 后端作为中间代理，将流式内容透传给前端。

## 1. 接口清单

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 宠物对话（流式） | POST | `/api/pet/chat` | 流式返回 AI 回答，保留多轮记忆 |
| 清空对话记忆 | DELETE | `/api/pet/chat/history/:sessionId` | 清空指定会话的历史记录 |

---

## 2. 宠物对话接口

### 2.1 请求

- **URL**: `POST /api/pet/chat`
- **Content-Type**: `application/json`
- **Accept**: `text/event-stream`（建议显式声明）

#### 请求体参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | string | 是 | 用户本轮输入内容 |
| sessionId | string | 否 | 会话 ID，用于多轮记忆隔离。不传时服务端自动生成（如 `pet-1700000000000`），前端应保存响应中的 `sessionId` 用于后续多轮 |

#### 请求示例

```bash
curl -N -X POST http://localhost:3000/api/pet/chat \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"message":"我家猫咪三个月大，该打什么疫苗？","sessionId":"pet-user-123"}'
```

> `-N` 表示不使用缓冲，立即输出流式内容。

### 2.2 响应

- **Content-Type**: `text/event-stream`
- **Cache-Control**: `no-cache`
- **Connection**: `keep-alive`

响应为 SSE 流，由多段 `data:` 行组成，每个事件之间用空行 `\n\n` 分隔。

### 2.3 事件类型

每个 `data:` 载荷均为 JSON，按 `type` 字段区分：

| type | 出现次数 | 说明 | 载荷字段 |
|------|---------|------|----------|
| `chunk` | 多次（每个 token 一段） | AI 流式输出的增量内容 | `content`(string), `sessionId`(string) |
| `complete` | 1 次（成功时） | 整轮对话结束的完整结果 | `data`({success, reply, sessionId}), `sessionId` |
| `error` | 1 次（失败时） | 错误信息 | `message`(string), `sessionId` |

另外，正常结束时还会发送一个 SSE 结束事件：

```
event:end
data:{"done":true}
```

### 2.4 响应示例（流式）

```
data: {"type":"chunk","content":"三个月","sessionId":"pet-user-123"}

data: {"type":"chunk","content":"的","sessionId":"pet-user-123"}

data: {"type":"chunk","content":"猫咪","sessionId":"pet-user-123"}

...

data: {"type":"complete","data":{"success":true,"reply":"三个月的猫咪...（完整回复）","sessionId":"pet-user-123"},"sessionId":"pet-user-123"}

event:end
data:{"done":true}
```

### 2.5 错误响应

错误时通过 SSE 流的 `error` 事件返回（HTTP 状态码仍为 200，因为响应头已发送为 SSE）：

```
data: {"type":"error","message":"API key 不正确","sessionId":"pet-user-123"}
```

参数校验错误（如未传 `message`）会直接返回普通 JSON，状态码 400：

```json
{
  "success": false,
  "code": 400,
  "message": "请提供对话消息"
}
```

---

## 3. 清空对话记忆接口

### 3.1 请求

- **URL**: `DELETE /api/pet/chat/history/:sessionId`
- **Path 参数**: `sessionId` 要清空的会话 ID

#### 请求示例

```bash
curl -X DELETE http://localhost:3000/api/pet/chat/history/pet-user-123
```

### 3.2 响应示例

```json
{
  "success": true,
  "code": 200,
  "message": "对话记忆已清空",
  "sessionId": "pet-user-123"
}
```

---

## 4. 多轮记忆机制说明

- 服务端以 `sessionId` 为 key 在内存中维护对话历史（包含用户输入与 AI 回复）。
- 每轮对话时，会把历史消息连同本轮一起发送给 LLM，从而实现"上下文记忆"。
- 默认保留最近 20 条消息（`HumanMessage` 与 `AIMessage` 各算一条），超过则自动淘汰最早的消息，避免 token 超限。
- **⚠️ 注意**：当前为单机内存存储，服务重启会丢失。如需多实例部署或持久化，请改造为 Redis 等存储（见 [baseAIService.js](file:///Users/caimingyang/Documents/code/travel-server/src/services/baseAIService.js) 中的 `this.histories`）。

---

## 5. Spring Boot 调用示例

推荐使用 Spring WebFlux 的 `WebClient` 调用 SSE 接口，并把流原样透传给前端（前端用 `EventSource` 消费）。

### 5.1 Maven 依赖

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

> 若使用 Spring MVC（非 WebFlux），可用 `WebClient` 调用，Controller 返回 `SseEmitter` 透传。下面两种方式都给出。

### 5.2 配置

```yaml
# application.yml
pet:
  api:
    base-url: http://localhost:3000
    chat-path: /api/pet/chat
    history-path: /api/pet/chat/history
```

```java
@Configuration
public class PetApiConfig {
    @Value("${pet.api.base-url}") private String baseUrl;
    @Value("${pet.api.chat-path}") private String chatPath;
    @Value("${pet.api.history-path}") private String historyPath;

    @Bean
    public WebClient petWebClient() {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    public String getChatPath() { return chatPath; }
    public String getHistoryPath() { return historyPath; }
}
```

### 5.3 Service 层：透传 SSE 流（WebFlux）

```java
@Service
public class PetChatService {

    private final WebClient webClient;
    private final PetApiConfig config;

    public PetChatService(WebClient petWebClient, PetApiConfig config) {
        this.webClient = petWebClient;
        this.config = config;
    }

    /**
     * 调用 Node 端流式接口，原样返回 Flux<String>，每个元素是一段 SSE 的 data 行
     * 前端 Controller 可直接用 Flux<SseEvent> 或 text/event-stream 返回
     */
    public Flux<String> chatStream(String message, String sessionId) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        if (sessionId != null && !sessionId.isBlank()) {
            body.put("sessionId", sessionId);
        }

        return webClient.post()
                .uri(config.getChatPath())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)     // Spring 会把每条 data: 的内容转成一个 String
                .onErrorResume(e -> Flux.just("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
    }

    public void clearHistory(String sessionId) {
        webClient.delete()
                .uri(config.getHistoryPath() + "/" + sessionId)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}
```

### 5.4 Controller 层：返回 SSE 给前端

#### 方式 A：WebFlux 项目（推荐，最简洁）

```java
@RestController
@RequestMapping("/api/pet")
public class PetController {

    private final PetChatService petChatService;

    public PetController(PetChatService petChatService) {
        this.petChatService = petChatService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody PetChatReq req) {
        return petChatService.chatStream(req.getMessage(), req.getSessionId());
    }

    @DeleteMapping("/chat/history/{sessionId}")
    public ResponseEntity<Void> clear(@PathVariable String sessionId) {
        petChatService.clearHistory(sessionId);
        return ResponseEntity.noContent().build();
    }
}
```

#### 方式 B：Spring MVC 项目（用 SseEmitter 透传）

```java
@RestController
@RequestMapping("/api/pet")
public class PetController {

    private final PetChatService petChatService;

    public PetController(PetChatService petChatService) {
        this.petChatService = petChatService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody PetChatReq req) throws IOException {
        SseEmitter emitter = new SseEmitter(0L); // 不超时

        petChatService.chatStream(req.getMessage(), req.getSessionId())
            .doOnNext(data -> {
                try {
                    emitter.send(SseEmitter.event().data(data));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            })
            .doOnComplete(emitter::complete)
            .doOnError(emitter::completeWithError)
            .subscribe();

        return emitter;
    }

    @DeleteMapping("/chat/history/{sessionId}")
    public ResponseEntity<Void> clear(@PathVariable String sessionId) {
        petChatService.clearHistory(sessionId);
        return ResponseEntity.noContent().build();
    }
}
```

### 5.5 请求 DTO

```java
public class PetChatReq {
    private String message;
    private String sessionId;
    // getter / setter
}
```

### 5.6 前端如何消费

前端可直接用浏览器原生 `EventSource`（仅支持 GET）或用 `fetch` + `ReadableStream` 处理 POST 的 SSE：

```js
const resp = await fetch('/api/pet/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ message, sessionId }),
});
const reader = resp.body.getReader();
const decoder = new TextDecoder();
let buffer = '';
while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  buffer += decoder.decode(value, { stream: true });
  // 按 \n\n 切分 SSE 事件
  let idx;
  while ((idx = buffer.indexOf('\n\n')) >= 0) {
    const rawEvent = buffer.slice(0, idx);
    buffer = buffer.slice(idx + 2);
    const line = rawEvent.split('\n').find(l => l.startsWith('data:'));
    if (!line) continue;
    const payload = JSON.parse(line.slice(5).trim());
    // payload.type: 'chunk' | 'complete' | 'error'
    if (payload.type === 'chunk') {
      appendToUI(payload.content);
    } else if (payload.type === 'complete') {
      saveSessionId(payload.sessionId);
    } else if (payload.type === 'error') {
      showError(payload.message);
    }
  }
}
```

---

## 6. 测试

```bash
# 启动 Node 服务
npm run dev

# 第一轮
curl -N -X POST http://localhost:3000/api/pet/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"我家猫三个月大该打什么疫苗？","sessionId":"pet-test-1"}'

# 第二轮（带相同 sessionId，会带上上文）
curl -N -X POST http://localhost:3000/api/pet/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"那驱虫呢？","sessionId":"pet-test-1"}'

# 清空记忆
curl -X DELETE http://localhost:3000/api/pet/chat/history/pet-test-1
```

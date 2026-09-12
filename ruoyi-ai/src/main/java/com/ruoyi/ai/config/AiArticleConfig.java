package com.ruoyi.ai.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * 养宠知识文章生成配置（对接 Node.js 文章生成服务）
 *
 * 与对话服务同机部署，复用同一基础地址，仅路径不同。
 * 文章 + 配图生成耗时较长（20~40 秒），这里把响应超时放宽到 120 秒。
 *
 * 注意：Node 服务会把封面 + 内嵌配图以 base64 内嵌在 JSON 响应里返回，
 * 单篇文章最多 4 张图（1 封面 + 3 内嵌），1024 尺寸的 PNG base64 可达数十 MB，
 * 因此必须把 WebClient 的内存缓冲上限调大，否则会触发 DataBufferLimitException。
 *
 * @author ruoyi
 */
@Configuration
public class AiArticleConfig
{
    /** Node.js 文章生成服务基础地址 */
    @Value("${ai.article.base-url}")
    private String baseUrl;

    /** 生成文章接口路径 */
    @Value("${ai.article.generate-path}")
    private String generatePath;

    /** 能力探测接口路径 */
    @Value("${ai.article.status-path}")
    private String statusPath;

    /**
     * 调用 Node.js 文章生成服务的 WebClient（带 120 秒响应超时）
     *
     * 配图以 base64 内嵌在响应体中，单篇最多 4 张图，可能达数十 MB，
     * 因此显式放大内存缓冲上限到 64MB，避免 DataBufferLimitException。
     */
    @Bean
    public WebClient aiArticleWebClient()
    {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(120));

        // 默认 256KB 缓冲上限不够承载内嵌 base64 配图，放大到 64MB
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(64 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public String getGeneratePath()
    {
        return generatePath;
    }

    public String getStatusPath()
    {
        return statusPath;
    }
}

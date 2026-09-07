package com.ruoyi.framework.config;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 验证码配置
 * <p>
 * 使用预渲染字符图片生成验证码，完全不依赖 AWT Font 渲染，
 * 确保 Linux 服务器无 fontconfig 环境下也能正常工作。
 * </p>
 *
 * @author ruoyi
 */
@Configuration
public class CaptchaConfig
{
    @PostConstruct
    public void init()
    {
        // 强制开启 headless 模式（双重保险）
        System.setProperty("java.awt.headless", "true");
    }

    /**
     * 普通字符验证码 Producer
     */
    @Bean(name = "captchaProducer")
    public PreRenderedCaptchaProducer getKaptchaBean()
    {
        PreRenderedCaptchaProducer producer = new PreRenderedCaptchaProducer();
        producer.setWidth(160);
        producer.setHeight(60);
        producer.setCharLength(4);
        producer.setCharSpacing(6);
        producer.init();
        return producer;
    }

    /**
     * 数学验证码 Producer（同普通验证码，数学题逻辑在 Controller 中处理）
     * <p>
     * 数学题格式形如 12+5=?（最多 6 个字符），因此加宽画布避免字符被截断。
     * </p>
     */
    @Bean(name = "captchaProducerMath")
    public PreRenderedCaptchaProducer getKaptchaBeanMath()
    {
        PreRenderedCaptchaProducer producer = new PreRenderedCaptchaProducer();
        producer.setWidth(230);
        producer.setHeight(60);
        producer.setCharLength(4);
        producer.setCharSpacing(6);
        producer.init();
        return producer;
    }
}

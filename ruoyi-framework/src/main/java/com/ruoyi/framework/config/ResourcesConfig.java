package com.ruoyi.framework.config;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.framework.interceptor.RepeatSubmitInterceptor;

/**
 * 通用配置
 * 
 * @author ruoyi
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer
{
    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        /**
         * 本地文件上传路径
         * 说明：前端不同环境会拼上不同的 API 前缀再访问图片资源，
         *       为了保证 dev/prod/小程序 三端都能直接访问，这里同时注册多套映射：
         *       1) /profile/**              -> 标准路径（后端直接访问）
         *       2) /dev-api/profile/**      -> 管理后台开发环境（VUE_APP_BASE_API=/dev-api）
         *       3) /prod-api/profile/**     -> 管理后台生产环境（VUE_APP_BASE_API=/prod-api）
         *       4) /api/v1/profile/**       -> 小程序端接口前缀（VITE_API_PREFIX=/api/v1）
         */
        String resourceLocation = "file:" + RuoYiConfig.getProfile() + "/";
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations(resourceLocation);
        registry.addResourceHandler("/dev-api" + Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations(resourceLocation);
        registry.addResourceHandler("/prod-api" + Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations(resourceLocation);
        registry.addResourceHandler("/api/v1" + Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations(resourceLocation);

        /** swagger配置 */
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .setCacheControl(CacheControl.maxAge(5, TimeUnit.HOURS).cachePublic());
    }

    /**
     * 自定义拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter()
    {
        CorsConfiguration config = new CorsConfiguration();
        // 设置访问源地址
        config.addAllowedOriginPattern("*");
        // 设置访问源请求头
        config.addAllowedHeader("*");
        // 设置访问源请求方法
        config.addAllowedMethod("*");
        // 有效期 1800秒
        config.setMaxAge(1800L);
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);
    }
}
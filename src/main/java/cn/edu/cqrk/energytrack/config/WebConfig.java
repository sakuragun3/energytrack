package cn.edu.cqrk.energytrack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*") // 允许所有请求头
                .allowedMethods("*") // 允许所有 HTTP 方法
                .allowedOrigins("http://127.0.0.1:5500", "http://localhost:8080") // 添加前端和后端地址
                .allowCredentials(true)// 允许携带凭证
                .exposedHeaders("Content-Disposition"); // 重要：暴露Content-Disposition头


    }
}
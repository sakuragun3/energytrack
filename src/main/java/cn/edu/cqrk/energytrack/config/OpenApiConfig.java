package cn.edu.cqrk.energytrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList("JWT")) // 添加全局安全要求
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("JWT", securityScheme())); // 定义安全方案
    }

    private Info apiInfo() {
        return new Info()
                .title("远程电表监控系统 API")
                .description("API文档 - 如果没能一次成功，那就叫它1.0版吧")
                .version("1.0")
                .contact(new Contact()
                        .name("开发团队")
                        .email("dev@cqrk.edu.cn"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name("JWT")
                .type(SecurityScheme.Type.HTTP) // 使用 HTTP 认证
                .scheme("bearer") // Bearer 令牌
                .bearerFormat("JWT") // 指定 JWT 格式
                .in(SecurityScheme.In.HEADER) // 在 Header 中传递
                .description("请输入带有 'Bearer ' 前缀的 JWT 令牌，例如：Bearer eyJhbGciOiJIUzI1NiIsIn...");
    }
}
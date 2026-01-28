//package cn.edu.cqrk.energytrack.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.*;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spi.service.contexts.SecurityContext;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//import java.sql.Timestamp;
//import java.util.Collections;
//import java.util.List;
//
//@Configuration
//@EnableSwagger2
//public class SwaggerConfig {
//
//    private static final String TOKEN_HEADER = "Authorization";
//    private static final String TOKEN_PREFIX = "Bearer ";
//
//    @Bean
//    public Docket api() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(apiInfo())
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("cn.edu.cqrk.energytrack.controller"))
//                .paths(PathSelectors.any())
//                .build()
//                .ignoredParameterTypes(Timestamp.class)
//                .securitySchemes(securitySchemes())
//                .securityContexts(securityContexts());
//    }
//
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder()
//                .title("远程电表监控系统 API")
//                .description("API文档 - 如果没能一次成功，那就叫它1.0版吧")
//                .version("1.0")
//                .contact(new Contact("开发团队", "", "dev@cqrk.edu.cn"))
//                .license("Apache 2.0")
//                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
//                .build();
//    }
//
//    private List<SecurityScheme> securitySchemes() {
//        return Collections.singletonList(
//                new ApiKey("JWT", TOKEN_HEADER, "header")
//        );
//    }
//
//    private List<SecurityContext> securityContexts() {
//        return Collections.singletonList(
//                SecurityContext.builder()
//                        .securityReferences(defaultAuth())
//                        .forPaths(PathSelectors.any())
//                        .build()
//        );
//    }
//
//    private List<SecurityReference> defaultAuth() {
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{
//                new AuthorizationScope("global", "accessEverything")
//        };
//        return Collections.singletonList(
//                new SecurityReference("JWT", authorizationScopes)
//        );
//    }
//}
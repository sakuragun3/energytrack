package cn.edu.cqrk.energytrack;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * EnergyTrack 项目主启动类
 *
 * @SpringBootApplication 是 Spring Boot 的核心注解，包含三个主要注解：
 * 1. @Configuration：标识为配置类
 * 2. @EnableAutoConfiguration：启用自动配置
 * 3. @ComponentScan：组件扫描
 */
@SpringBootApplication
@MapperScan("cn.edu.cqrk.energytrack.mapper")
@EnableCaching // 启用 Spring Cache
public class MainApp {
    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(MainApp.class);
    }
}
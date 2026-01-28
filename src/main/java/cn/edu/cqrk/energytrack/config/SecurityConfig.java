package cn.edu.cqrk.energytrack.config;

import cn.edu.cqrk.energytrack.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // 声明这是一个配置类
@EnableWebSecurity // 启用 Spring Security 的 Web 安全特性
@EnableGlobalMethodSecurity(prePostEnabled = true) // 启用方法级权限控制，例如可以使用 @PreAuthorize 和 @PostAuthorize 注解
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // JWT 认证过滤器

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean // 将 SecurityFilterChain 声明为一个 Spring Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") // 抑制 Spring 的注入点自动装配检查警告，通常用于处理某些特定的注入场景
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and() // 启用 CORS (跨域资源共享)
                .csrf().disable() // 禁用 CSRF (跨站请求伪造) 保护，通常在前后端分离的项目中禁用
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 设置会话管理为无状态，因为使用 JWT 进行认证
                .and()
                .authorizeRequests() // 配置授权规则
                // 放行登录和注册接口
                .antMatchers("/sysUser/login", "/sysUser/register").permitAll() // 允许所有用户访问 /sysUser/login 和 /sysUser/register 接口
                // 放行 Swagger UI 和 API 文档相关路径
                .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // 允许所有用户访问 Swagger UI 和 API 文档相关路径
                // 其他请求需要认证
                .anyRequest().authenticated() // 除了上面放行的路径，其他所有请求都需要进行身份认证
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 在 UsernamePasswordAuthenticationFilter 之前添加自定义的 JWT 认证过滤器

        return http.build(); // 构建 SecurityFilterChain
    }

    @Bean // 将 PasswordEncoder 声明为一个 Spring Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 使用 BCrypt PasswordEncoder 进行密码加密
    }
}
package cn.edu.cqrk.energytrack.filter;

import cn.edu.cqrk.energytrack.common.JwtUtil;
import cn.edu.cqrk.energytrack.common.R;
import cn.edu.cqrk.energytrack.common.BizExceptionCode;
import cn.edu.cqrk.energytrack.entity.pojo.SysUser;
import cn.edu.cqrk.energytrack.mapper.SysUserMapper;
import cn.edu.cqrk.energytrack.service.SysUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component // 使用 @Component 注解将此类标记为 Spring 管理的组件，使其能够被 Spring 容器自动发现和创建
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 继承 OncePerRequestFilter，确保每个请求只经过此过滤器一次

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class); // 初始化 SLF4j 日志记录器

    private final JwtUtil jwtUtil; // 注入 JWT 工具类，用于 JWT 的生成和解析
    private final ObjectMapper objectMapper; // 注入 Jackson 的 ObjectMapper，用于将错误信息序列化为 JSON 响应
    private final SysUserMapper sysUserMapper; // 注入 SysUserMapper，用于从数据库中查询用户信息

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") // 抑制 Spring 在构造器注入时可能出现的警告
    public JwtAuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, SysUserMapper sysUserMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization"); // 从请求头中获取名为 "Authorization" 的请求头，通常用于携带 JWT
        logger.debug("接收到 Authorization 请求头: {}", token); // 记录接收到的 Authorization 请求头

        if (token != null && token.startsWith("Bearer ")) { // 判断请求头中是否存在 Token 且以 "Bearer " 开头（标准的 JWT 格式）
            try {
                token = token.substring(7); // 如果以 "Bearer " 开头，则截取掉前 7 个字符（"Bearer " 和一个空格），得到 JWT 本身
                logger.debug("提取到的 JWT: {}", token); // 记录提取到的 JWT
                Claims claims = jwtUtil.parseToken(token); // 使用 jwtUtil 解析 JWT，获取 JWT 中存储的声明信息 (payload)
                logger.debug("解析后的 Claims: {}", claims); // 记录解析后的 JWT 声明信息

                String username = claims.getSubject(); // 从 JWT 的声明信息中获取主题 (subject)，通常存储的是用户名
                SysUser user = sysUserMapper.selectByUsername(username); // 使用 SysUserMapper 根据用户名从数据库中查询用户信息

                if (user != null) { // 如果根据用户名找到了对应的用户
                    logger.debug("从数据库查询到的用户 {} 的密码：{}", username, user.getPassword()); // 记录从数据库查询到的用户密码（通常不建议在生产环境记录密码）
                    String role = claims.get("roles", String.class); // 从 JWT 的声明信息中获取 "roles" 属性，通常存储的是用户角色
                    logger.debug("提取到的角色: {}", role); // 记录提取到的用户角色
                    UsernamePasswordAuthenticationToken authentication = // 创建 Spring Security 的认证 Token
                            new UsernamePasswordAuthenticationToken(
                                    user, // 将查询到的 SysUser 对象设置为认证的主体 (principal)
                                    null, // 凭证 (credentials) 通常在 JWT 认证中不需要再次验证，可以设置为 null
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)) // 创建一个包含用户角色的权限列表，并添加 "ROLE_" 前缀，这是 Spring Security 推荐的做法
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication); // 将创建的认证信息设置到 Spring Security 的安全上下文中，表示当前用户已认证
                    logger.debug("为用户设置认证信息: {}", username); // 记录为用户设置认证信息
                } else { // 如果根据用户名未找到用户
                    logger.error("根据用户名 {} 未找到用户", username); // 记录错误信息
                    response.setContentType("application/json;charset=UTF-8"); // 设置响应内容类型为 JSON
                    response.getWriter().write(objectMapper.writeValueAsString( // 将包含用户未找到错误信息的 R 对象序列化为 JSON 并写入响应
                            R.fail(BizExceptionCode.USER_NOT_FOUND)));
                    return; // 结束当前过滤器的处理，不再继续执行后续的过滤器
                }

            } catch (Exception e) { // 捕获 JWT 解析或验证过程中发生的异常
                logger.error("JWT 解析或验证失败: {}", e.getMessage()); // 记录 JWT 解析或验证失败的错误信息
                response.setContentType("application/json;charset=UTF-8"); // 设置响应内容类型为 JSON
                response.getWriter().write(objectMapper.writeValueAsString( // 将包含无效 Token 错误信息的 R 对象序列化为 JSON 并写入响应
                        R.fail(BizExceptionCode.INVALID_TOKEN)));
                return; // 结束当前过滤器的处理，不再继续执行后续的过滤器
            }
        }
        chain.doFilter(request, response); // 如果请求头中没有有效的 JWT，则将请求传递给过滤器链中的下一个过滤器
    }
}
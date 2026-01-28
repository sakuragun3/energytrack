package cn.edu.cqrk.energytrack.common;

import cn.edu.cqrk.energytrack.config.JwtProperties;
import cn.edu.cqrk.energytrack.entity.pojo.SysUser;
import cn.edu.cqrk.energytrack.entity.vo.SysUserVo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component // 声明这是一个 Spring 组件，可以被 Spring 容器管理
public class JwtUtil {

    private final JwtProperties jwtProperties; // 注入 JWT 相关配置属性
    private final SecretKey signingKey; // 用于签名 JWT 的密钥

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()); // 使用配置中的密钥生成 SecretKey 对象
    }

    @Value("${jwt.secret}") // 从配置文件中读取 jwt.secret 属性
    private String secret;
    @Value("${jwt.expiration}") // 从配置文件中读取 jwt.expiration 属性
    private Long expiration;

//    public String generateToken(SysUser user) {
//        return generateToken(user.getId(), user.getUsername(), user.getRole());
//    }

    public String generateToken(SysUserVo vo) {
        return generateToken(vo.getId(), vo.getUsername(), vo.getRole()); // 根据 SysUserVo 对象生成 JWT
    }

    private String generateToken(Integer userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>(); // 创建一个 Map 用于存放 JWT 的声明信息
        claims.put("userId", userId); // 存放用户 ID
        claims.put("username", username); // 存放用户名
        claims.put("roles", role); // 存放用户角色

        return Jwts.builder() // 开始构建 JWT
                .setClaims(claims) // 设置声明信息
                .setSubject(username) // 设置 JWT 的主题为用户名
                .setIssuedAt(new Date()) // 设置 JWT 的签发时间为当前时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000)) // 设置 JWT 的过期时间
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 使用签名密钥和 HS256 算法进行签名
                .compact(); // 将 JWT 构建成紧凑的字符串
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder() // 开始构建 JWT 解析器
                .setSigningKey(getSigningKey()) // 设置用于验证签名的密钥
                .build() // 构建解析器
                .parseClaimsJws(token) // 解析 JWT 字符串
                .getBody(); // 获取 JWT 的载荷 (Claims)
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes()); // 根据 secret 字符串生成 SecretKey 对象，用于签名和验证
    }
}
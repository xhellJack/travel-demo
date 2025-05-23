package com.example.travel.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {
    private static final String SECRET = "9ja$!kd92Lqp&fWL8sdKf@3KDls!sdfksjfoAjsdfsj1LSkqpweu9lSKDlsdkflKsd";
    private static final long EXPIRATION_TIME = 864_000_00; // 1 天

    private final SecretKey secretKey = Keys.hmacShaKeyFor("9ja$!kd92Lqp&fWL8sdKf@3KDls!sdfksjfoAjsdfsj1LSkqpweu9lSKDlsdkflKsd".getBytes());
    private final long expirationTime = 1000 * 60 * 60 * 10; // 10 小时 (毫秒)
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
                .compact();
    }

    public static String validateTokenAndGetUsername(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET.getBytes())
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null; // Token 无效或过期
        }
    }

    // 从 Token 中提取用户名 (主题)
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 从 Token 中提取权限/角色
    @SuppressWarnings("unchecked") // 对于 claims.get("authorities") 的转换
    public List<GrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        List<String> authoritiesString = claims.get("authorities", List.class);
        if (authoritiesString == null || authoritiesString.isEmpty()) {
            // 如果token中没有权限信息，可以返回空列表或默认权限
            return List.of(new SimpleGrantedAuthority("ROLE_USER")); // 示例：默认给个ROLE_USER
        }
        return authoritiesString.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // 验证 Token 是否有效
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // 检查 Token 是否过期
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // 从 Token 中提取所有声明
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}


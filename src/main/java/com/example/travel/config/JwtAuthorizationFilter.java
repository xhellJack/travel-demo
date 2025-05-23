package com.example.travel.config; // 和你的 SecurityConfig 在同一个包或其子包

import com.example.travel.config.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService; // <<< 注入你的 UserDetailsService 实现

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt); // 从JWT中提取用户名
            } catch (ExpiredJwtException e) {
                logger.warn("JWT token is expired: {}", e.getMessage(), e);
            } catch (Exception e) { // 更通用的异常捕获，例如 SignatureException, MalformedJwtException
                logger.warn("JWT token validation/parsing error: {}", e.getMessage(), e);
            }
        }

        // 当 Token 存在，解析出用户名，并且当前 SecurityContext 中没有认证信息时
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 1. 使用 UserDetailsService 加载 UserDetails
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 2. 验证 Token 是否仍然有效 (例如检查签名、过期时间，并确保Token中的用户与加载的UserDetails匹配)
                //    jwtUtil.validateToken() 方法需要能处理这个逻辑
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) { // 确保 validateToken 的第二个参数是你期望的比较对象

                    // 3. 创建 Authentication 对象，Principal 现在是 UserDetails 对象
                    //    权限直接从 UserDetails 对象中获取
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, // <<< Principal 是 UserDetails 对象
                                    null,        // 密码字段为 null，因为我们是基于 Token 认证
                                    userDetails.getAuthorities()); // <<< 权限来自 UserDetails

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 4. 将 Authentication 对象设置到 SecurityContext 中
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    logger.debug("Authenticated user {}, setting security context with UserDetails principal", username);
                } else {
                    logger.warn("JWT Token is not valid for user loaded: {}", username);
                }
            } catch (UsernameNotFoundException e) {
                logger.warn("User '{}' not found in UserDetailsService based on JWT subject.", username, e);
            } catch (Exception e) {
                // 其他加载 UserDetails 或验证过程中可能发生的异常
                logger.error("Error setting up user authentication from JWT: {}", e.getMessage(), e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
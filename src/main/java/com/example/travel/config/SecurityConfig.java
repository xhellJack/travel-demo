package com.example.travel.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    AuthenticationConfiguration authenticationConfiguration;
    @Autowired
    JwtAuthorizationFilter jwtAuthorizationFilter;
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico" // 如果需要允许访问网站图标
    };



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF (新版写法)
                .httpBasic(AbstractHttpConfigurer::disable) // 禁用 basic auth
                .formLogin(AbstractHttpConfigurer::disable) // 禁用 form 登录
                // 不需要 session (如果使用 token-based auth)
                // .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        // 公开访问的路径，例如登录、注册等
                        .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                        // 需要认证的路径，例如获取当前用户信息
                        .requestMatchers("/api/users/me").authenticated()
                        // 其他所有未明确指定的请求都需要认证 (这是一个好的默认安全策略)
                        .anyRequest().authenticated()
                );

        // 在 UsernamePasswordAuthenticationFilter (或类似的基础认证过滤器) 之前添加你的自定义认证过滤器

        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



}

package com.example.travel.service; // 假设你的服务类在这个包下

import com.example.travel.entity.User; // 引入你的 User 实体
import com.example.travel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 推荐在服务方法上使用事务

import java.util.List;
import java.util.stream.Collectors;
@Primary
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // 通常加载用户信息是只读操作
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 注意：这里的 User 是 com.example.travel.entity.User，你的JPA实体
        com.example.travel.entity.User domainUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 将存储的字符串角色集合转换为 Spring Security 需要的 GrantedAuthority 集合
        // 你的 User 实体中 @PrePersist 确保了 roles.add("ROLE_USER");
        // 所以我们假设 Set<String> roles 中存储的已经是 "ROLE_USER", "ROLE_ADMIN" 这样的字符串
        List<GrantedAuthority> authorities = domainUser.getRoles().stream()
                .map(roleString -> new SimpleGrantedAuthority(roleString))
                .collect(Collectors.toList());

        // 返回 Spring Security 内建的 UserDetails 实现
        // org.springframework.security.core.userdetails.User
        return new org.springframework.security.core.userdetails.User(
                domainUser.getUsername(),
                domainUser.getPassword(),
                domainUser.isActive(),   // enabled - 使用你 User 实体中的 active 字段
                true,                    // accountNonExpired - 假设为 true，你可以根据需要从 User 实体添加字段
                true,                    // credentialsNonExpired - 假设为 true
                true,                    // accountNonLocked - 假设为 true
                authorities);            // 用户的权限集合
    }
}
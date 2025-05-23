package com.example.travel.repository;

import com.example.travel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query; // Was present, but the example query was commented out
import org.springframework.data.repository.query.Param; // Was present, for the commented out query

import java.util.List; // Was present, for the commented out query
import java.util.Optional;

// @Repository // Spring Data JPA repositories are automatically detected, @Repository is optional
public interface UserRepository extends JpaRepository<User, Long> , JpaSpecificationExecutor<User> { // Assuming ID is Long now

    // 根据用户名查找用户（用于登录和 UserDetailsService）
    Optional<User> findByUsername(String username);

    // 检查用户名是否已存在 (用于注册时校验)
    boolean existsByUsername(String username);

    // 检查邮箱是否已存在 (用于注册和更新时校验)
    boolean existsByEmail(String email);

    // 检查邮箱是否已存在，但排除指定ID的用户 (用于更新用户信息时校验邮箱唯一性)
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByUsernameAndIdNot(String username, Long id);

    //--analysis

    @Query("SELECT u.gender, COUNT(u) FROM User u WHERE u.gender IS NOT NULL GROUP BY u.gender")
    List<Object[]> countUsersByGender();

    @Query("SELECT t.name, COUNT(u.id) FROM User u JOIN u.preferredTags t GROUP BY t.name ORDER BY COUNT(u.id) DESC")
    Page<Object[]> findTopUserPreferredTags(Pageable pageable);
}
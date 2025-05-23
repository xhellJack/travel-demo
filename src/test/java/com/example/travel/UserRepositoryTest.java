package com.example.travel;

import com.example.travel.entity.User;
import com.example.travel.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@SpringBootTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByUsername() {
        User user = userRepository.findByUsername("admin2")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        System.out.println(user);
    }
}
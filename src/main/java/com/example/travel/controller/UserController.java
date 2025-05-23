package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.*; // Import all relevant DTOs
import com.example.travel.entity.User; // Will mostly be replaced by DTOs in controller layer
import com.example.travel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For method-level security
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // Another way to get current user

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 用户注册 (Public)
    @PostMapping("/register")
    public ResponseEntity<Result<UserResponse>> register(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        UserResponse registeredUser = userService.register(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(registeredUser));
    }

    // 用户登录 (Public)
    // 通常登录接口会放在一个专门的 AuthController，并可能返回 JWT Token 直接在响应体中
    // 如果 userService.login 返回 AuthResponse，我们可以这样：
    @PostMapping("/login")
    public ResponseEntity<Result<AuthResponse>> login(@Valid @RequestBody UserLoginRequest loginRequest) {
        AuthResponse authResponse = userService.login(loginRequest);
        return ResponseEntity.ok(Result.success(authResponse));
    }

    // 获取当前登录用户信息 (Authenticated users)
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Ensures the user is authenticated
    public ResponseEntity<Result<UserResponse>> getCurrentUser(Authentication authentication) {
        // Authentication object is injected by Spring Security
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUserEntity = userService.findUserEntityByUsername(userDetails.getUsername()); // Assuming this method exists
        return ResponseEntity.ok(Result.success(userService.convertToUserResponse(currentUserEntity))); // Assuming this DTO converter exists
    }

    // 或者，让UserService处理获取当前登录用户的逻辑
    // @GetMapping("/me")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<Result<UserResponse>> getCurrentUserPrincipal(Principal principal) {
    //     UserResponse userResponse = userService.getUserResponseByUsername(principal.getName());
    //     return ResponseEntity.ok(Result.success(userResponse));
    // }


    // 根据ID获取用户详情 (Admin or user themselves)
    @GetMapping("/{id}")
    // Example: Allow admin or the user themselves to get their details
    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(authentication, #id)")
    public ResponseEntity<Result<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserResponseById(id);
        return ResponseEntity.ok(Result.success(userResponse));
    }

    // 更新用户信息 (User can update their own, Admin can update any)
    @PutMapping("/{id}")
    // Example: Allow admin or the user themselves to update their details
    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(authentication, #id)")
    public ResponseEntity<Result<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest updateRequest,
            Authentication authentication) {
        // Optional: You might want to ensure 'id' from path matches a potential 'id' in UserUpdateRequest
        // Or ensure that a non-admin user can only update their own profile using their own ID.
        // The @PreAuthorize already handles the basic "isSelf" or "isAdmin" check.
        UserResponse updatedUser = userService.updateUser(id, updateRequest);
        return ResponseEntity.ok(Result.success(updatedUser));
    }


    // 删除用户 (Admin only)
    // 在实际应用中，删除用户通常是软删除 (is_active = false) 而不是物理删除
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can delete users directly via this endpoint
    public ResponseEntity<Result<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Result.success(null)); // Or ResponseEntity.noContent()
    }

    // 注意: 获取所有用户列表通常是管理员功能，并且应该分页。
    // 这个功能通常会放在 AdminUserController 中。
    // 如果要在这里实现，也需要分页和权限控制。
    // 例如:
    // @GetMapping
    // @PreAuthorize("hasRole('ADMIN')")
    // public ResponseEntity<Result<Page<UserResponse>>> getAllUsers(Pageable pageable) {
    //     Page<UserResponse> users = userService.getAllUsers(pageable); // Assuming UserService returns Page<UserResponse>
    //     return ResponseEntity.ok(Result.success(users));
    // }
}
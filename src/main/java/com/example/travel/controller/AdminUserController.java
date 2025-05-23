package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.UserResponse;
import com.example.travel.dto.UserUpdateByAdminRequest; // New DTO
import com.example.travel.service.IUserService;
import com.example.travel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')") // Apply to all methods in this controller
public class AdminUserController {

    private final UserService userService;

    @Autowired
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // 分页获取所有用户列表 (可按用户名筛选)
    @GetMapping
    public ResponseEntity<Result<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String username, // Optional username filter
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort[1].equalsIgnoreCase("desc") ? "DESC" : "ASC");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<UserResponse> usersPage;
        if (username != null && !username.trim().isEmpty()) {
            // Assuming UserService has a method like:
            // Page<UserResponse> searchUsersByUsername(String username, Pageable pageable);
            // For now, let's use a placeholder or assume filtering is part of a more general search in UserService
            // For simplicity, if filtering by username is simple, userService.getAllUsers could take an optional username
            // Or we rely on a more complex search method in userService.
            // Let's assume userService.getAllUsers or a new search method handles this.
            // For this example, I'll call a hypothetical search method.
            // usersPage = userService.searchUsers(username, pageable);
            // If userService.getAllUsers(pageable) doesn't support search, we'd need to enhance it or add a new service method.
            // For now, let's assume a simplified getAllUsers which may or may not include search.
            // The original code didn't show how username filter was applied in service.
            usersPage = userService.getAllUsers(pageable, username); // Assuming service method is updated
        } else {
            usersPage = userService.getAllUsers(pageable, null); // Assuming service method is updated
        }
        return ResponseEntity.ok(Result.success(usersPage));
    }

    // 管理员更新指定用户信息
    @PutMapping("/{id}")
    public ResponseEntity<Result<UserResponse>> updateUserByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateByAdminRequest updateRequest) {
        // The UserService will need a specific method to handle updates by admin
        // as it might involve changing fields regular users cannot (like roles, isActive)
        UserResponse updatedUser = userService.updateUserByAdmin(id, updateRequest);
        return ResponseEntity.ok(Result.success(updatedUser));
    }

    // 获取单个用户详情 (Admin can get any user)
    // This is same as UserController.getUserById, just ensured here it's admin only.
    @GetMapping("/{id}")
    public ResponseEntity<Result<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserResponseById(id);
        return ResponseEntity.ok(Result.success(userResponse));
    }

    // 删除用户 (Admin can delete any user)
    // This is same as UserController.deleteUser, just ensured here it's admin only.
    // (Reminder: Consider soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Result.success(null));
    }

    // (Optional: Admin creating a user - might be less common than self-registration)
    // @PostMapping
    // public ResponseEntity<Result<UserResponse>> createUserByAdmin(
    //         @Valid @RequestBody UserCreateByAdminRequest createRequest) {
    //     UserResponse createdUser = userService.createUserByAdmin(createRequest);
    //     return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(createdUser));
    // }
}
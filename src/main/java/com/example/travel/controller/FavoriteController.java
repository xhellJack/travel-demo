package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.AttractionResponse;
import com.example.travel.dto.FavoriteRequest; // We created this DTO earlier
import com.example.travel.entity.User; // For getting current user from UserService if needed
import com.example.travel.service.FavoriteService;
import com.example.travel.service.UserService; // To get current user details
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService; // To get current authenticated user

    @Autowired
    public FavoriteController(FavoriteService favoriteService, UserService userService) {
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    // 添加收藏 (Authenticated users can favorite for themselves)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<Void>> addFavorite(
            @Valid @RequestBody FavoriteRequest favoriteRequest,
            Authentication authentication) {

        User currentUser = userService.getCurrentAuthenticatedUser(); // Get currently authenticated user
        favoriteService.addFavorite(currentUser.getId(), favoriteRequest.getAttractionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(null));
    }

    // 从当前用户的收藏中移除指定景点 (Authenticated users can unfavorite for themselves)
    @DeleteMapping("/attractions/{attractionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<Void>> removeFavorite(
            @PathVariable Long attractionId,
            Authentication authentication) {

        User currentUser = userService.getCurrentAuthenticatedUser();
        favoriteService.removeFavorite(currentUser.getId(), attractionId);
        return ResponseEntity.ok(Result.success(null));
    }

    // 获取当前登录用户的收藏列表 (Authenticated users)
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<Page<AttractionResponse>>> getCurrentUserFavorites(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) { // Sort by favorite creation time by default

        User currentUser = userService.getCurrentAuthenticatedUser();

        Sort.Direction direction = Sort.Direction.fromString(sort[1].equalsIgnoreCase("desc") ? "DESC" : "ASC");
        Sort.Order order = new Sort.Order(direction, sort[0]); // Sort by UserFavorite.createdAt
        // If sorting by Attraction fields, the query in FavoriteService/Repository needs to support it.
        // For now, FavoriteService.getUserFavorites returns attractions, so sorting on UserFavorite fields
        // needs to be handled before fetching attractions or the query needs to join and sort.
        // Let's assume for now Pageable is passed and FavoriteService handles it.
        // If UserFavorite.createdAt needs to be sorted, the repository method for UserFavorite must support it.
        // Let's use a default sort that makes sense for favorites, e.g., when they were added.
        // The `FavoriteService.getUserFavorites` takes Pageable, so we pass it on.
        // Sorting by "createdAt" here refers to UserFavorite.createdAt.

        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        Page<AttractionResponse> favorites = favoriteService.getUserFavorites(currentUser.getId(), pageable);
        return ResponseEntity.ok(Result.success(favorites));
    }


    // 获取指定用户的收藏列表 (Admin only, or if public profiles feature exists)
    // For now, let's make it admin-only to avoid complexity of public user profiles
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Page<AttractionResponse>>> getUserFavoritesByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort[1].equalsIgnoreCase("desc") ? "DESC" : "ASC");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<AttractionResponse> favorites = favoriteService.getUserFavorites(userId, pageable);
        return ResponseEntity.ok(Result.success(favorites));
    }
}
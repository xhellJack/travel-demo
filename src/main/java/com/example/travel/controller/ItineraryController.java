package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.*; // Import all relevant DTOs
import com.example.travel.entity.User; // For getting current user
import com.example.travel.service.ItineraryService;
import com.example.travel.service.UserService; // To get current user
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
@RequestMapping("/api/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final UserService userService; // To get current user

    @Autowired
    public ItineraryController(ItineraryService itineraryService, UserService userService) {
        this.itineraryService = itineraryService;
        this.userService = userService;
    }

    // 创建新行程 (Authenticated users can create for themselves)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<ItineraryResponse>> createItinerary(
            @Valid @RequestBody ItineraryCreateRequest createRequest,
            Authentication authentication) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        ItineraryResponse itineraryResponse = itineraryService.createItinerary(createRequest, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(itineraryResponse));
    }

    // 根据ID获取行程详情 (Public if itinerary is public, otherwise owner/admin)
    @GetMapping("/{id}")
    public ResponseEntity<Result<ItineraryResponse>> getItineraryById(@PathVariable Long id) {
        // Permission check logic is now handled by ItineraryService.getItineraryById or via @PostAuthorize
        // For simplicity, we assume service handles or it's public access initially,
        // or use @PostAuthorize("#returnObject.body.data.user.id == authentication.principal.id or #returnObject.body.data.isPublic or hasRole('ADMIN')")
        // For now, let's keep it simple and assume service might handle some checks, or it's mostly public view
        ItineraryResponse itineraryResponse = itineraryService.getItineraryById(id);
        return ResponseEntity.ok(Result.success(itineraryResponse));
    }

    // 更新行程信息 (Owner or Admin)
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Further authorization within service layer
    public ResponseEntity<Result<ItineraryResponse>> updateItinerary(
            @PathVariable Long id,
            @Valid @RequestBody ItineraryUpdateRequest updateRequest,
            Authentication authentication) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        // The service method will handle if currentUser.getId() is allowed to update this itinerary
        ItineraryResponse updatedItinerary = itineraryService.updateItinerary(id, updateRequest, currentUser.getId());
        return ResponseEntity.ok(Result.success(updatedItinerary));
    }

    // 删除行程 (Owner or Admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Further authorization within service layer
    public ResponseEntity<Result<Void>> deleteItinerary(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        itineraryService.deleteItinerary(id, currentUser.getId());
        return ResponseEntity.ok(Result.success(null));
    }

    // 获取当前登录用户的所有行程 (Authenticated users)
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<Page<ItineraryBasicResponse>>> getCurrentUserItineraries(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String[] sort) {

        User currentUser = userService.getCurrentAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(sort[1].equalsIgnoreCase("desc") ? "DESC" : "ASC");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<ItineraryBasicResponse> itineraries = itineraryService.getItinerariesByUserId(currentUser.getId(), pageable);
        return ResponseEntity.ok(Result.success(itineraries));
    }

    // 获取指定用户的所有行程 (Admin only for now, or if profiles are public and itineraries are public)
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // Example: Admin only
    public ResponseEntity<Result<Page<ItineraryBasicResponse>>> getUserItineraries(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort[1].equalsIgnoreCase("desc") ? "DESC" : "ASC");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<ItineraryBasicResponse> itineraries = itineraryService.getItinerariesByUserId(userId, pageable);
        return ResponseEntity.ok(Result.success(itineraries));
    }

    // 获取所有公开的行程 (Public)
    @GetMapping("/public")
    public ResponseEntity<Result<Page<ItineraryBasicResponse>>> getPublicItineraries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort[1].equalsIgnoreCase("desc") ? "DESC" : "ASC");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<ItineraryBasicResponse> itineraries = itineraryService.getPublicItineraries(pageable);
        return ResponseEntity.ok(Result.success(itineraries));
    }
}
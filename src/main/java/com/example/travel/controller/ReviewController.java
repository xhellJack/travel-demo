package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.ReviewRequest;
import com.example.travel.dto.ReviewResponse;
import com.example.travel.entity.User; // For getting current user
import com.example.travel.service.ReviewService;
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
@RequestMapping("/api") // Base path, specific paths for attractions/users for reviews
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @Autowired
    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    // 创建新评价 (Authenticated users)
    // Endpoint: POST /api/reviews - DTO contains attractionId
    @PostMapping("/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewRequest reviewRequest,
            Authentication authentication) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        ReviewResponse reviewResponse = reviewService.createReview(reviewRequest, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(reviewResponse));
    }

    // 获取单个评价详情 (Public)
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<Result<ReviewResponse>> getReviewById(@PathVariable Long reviewId) {
        ReviewResponse reviewResponse = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(Result.success(reviewResponse));
    }

    // 获取某个景点的所有评价 (Public, paginated)
    @GetMapping("/attractions/{attractionId}/reviews")
    public ResponseEntity<Result<Page<ReviewResponse>>> getReviewsForAttraction(
            @PathVariable Long attractionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort[1].equalsIgnoreCase("desc") ? "DESC" : "ASC");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<ReviewResponse> reviewsPage = reviewService.getReviewsByAttractionId(attractionId, pageable);
        return ResponseEntity.ok(Result.success(reviewsPage));
    }

    // 获取当前登录用户的所有评价 (Authenticated users, paginated)
    @GetMapping("/reviews/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<Page<ReviewResponse>>> getCurrentUserReviews(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        Sort.Direction direction = Sort.Direction.fromString(sort[1].equalsIgnoreCase("desc") ? "DESC" : "ASC");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<ReviewResponse> reviewsPage = reviewService.getReviewsByUserId(currentUser.getId(), pageable);
        return ResponseEntity.ok(Result.success(reviewsPage));
    }

    // 获取指定用户的评价 (Admin only for now, paginated)
    @GetMapping("/users/{userId}/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Page<ReviewResponse>>> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort[1].equalsIgnoreCase("desc") ? "DESC" : "ASC");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<ReviewResponse> reviewsPage = reviewService.getReviewsByUserId(userId, pageable);
        return ResponseEntity.ok(Result.success(reviewsPage));
    }


    // 更新评价 (Owner of the review)
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()") // Further check in service layer: review.getUser().getId().equals(currentUser.getId())
    public ResponseEntity<Result<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest reviewRequest, // ReviewRequest may need to exclude attractionId for updates
            Authentication authentication) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        // The reviewRequest DTO for update typically should not contain attractionId,
        // as one usually doesn't change which attraction a review is for.
        // If ReviewRequest always has attractionId, the service should ignore it on update.
        ReviewResponse updatedReview = reviewService.updateReview(reviewId, reviewRequest, currentUser.getId());
        return ResponseEntity.ok(Result.success(updatedReview));
    }

    // 删除评价 (Owner of the review or Admin)
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()") // Further check in service layer
    public ResponseEntity<Result<Void>> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        // Service layer will check if current user is owner or admin
        reviewService.deleteReview(reviewId, currentUser.getId());
        return ResponseEntity.ok(Result.success(null));
    }

    // 增加评价的“有用”计数 (Authenticated users)
    @PostMapping("/reviews/{reviewId}/helpful")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<ReviewResponse>> markReviewAsHelpful(@PathVariable Long reviewId) {
        // Note: A user should ideally only be able to mark a review as helpful once.
        // This would require tracking (e.g., a UserReviewHelpfulness join table).
        // For simplicity, this endpoint just increments.
        ReviewResponse reviewResponse = reviewService.incrementHelpfulCount(reviewId);
        return ResponseEntity.ok(Result.success(reviewResponse));
    }
}
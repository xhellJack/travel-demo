package com.example.travel.service;

import com.example.travel.dto.AttractionBasicResponse; // Needed for ReviewResponse
import com.example.travel.dto.ReviewRequest;
import com.example.travel.dto.ReviewResponse;
import com.example.travel.dto.UserBasicResponse; // Needed for ReviewResponse
import com.example.travel.entity.Attraction;
import com.example.travel.entity.Review;
import com.example.travel.entity.User;
import com.example.travel.exception.ConflictException;
import com.example.travel.exception.ForbiddenException;
import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService; // To get User entity and UserBasicResponse
    private final AttractionService attractionService; // To get Attraction entity, AttractionBasicResponse, and update ratings

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         UserService userService,
                         AttractionService attractionService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.attractionService = attractionService;
    }

    // --- DTO Converter ---
    public ReviewResponse convertToReviewResponse(Review review) {
        if (review == null) {
            return null;
        }
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setTitle(review.getTitle());
        response.setComment(review.getComment());
        response.setVisitDate(review.getVisitDate());
        response.setImageUrl(review.getImageUrl());
        response.setHelpfulCount(review.getHelpfulCount());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());

        // Populate basic user and attraction info
        if (review.getUser() != null) {
            // Assuming UserService has a method to convert User to UserBasicResponse
            // or we create one here if UserBasicResponse is simple enough.
            // For now, let's assume a simple mapping if UserService doesn't expose it.
            UserBasicResponse userDto = new UserBasicResponse(
                    review.getUser().getId(),
                    review.getUser().getUsername(),
                    review.getUser().getAvatar()
            );
            response.setUser(userDto);
        }
        if (review.getAttraction() != null) {
            // Assuming AttractionService has a method to convert Attraction to AttractionBasicResponse
            // or we create one here if AttractionBasicResponse is simple enough.
            AttractionBasicResponse attractionDto = new AttractionBasicResponse(
                    review.getAttraction().getId(),
                    review.getAttraction().getName(),
                    review.getAttraction().getImageUrl(),
                    review.getAttraction().getCategory(),
                    review.getAttraction().getLocation(),
                    review.getAttraction().getAverageRating()
            );
            response.setAttraction(attractionDto);
        }
        return response;
    }

    private Page<ReviewResponse> convertToReviewResponsePage(Page<Review> reviewPage) {
        return reviewPage.map(this::convertToReviewResponse);
    }

    // --- Service Methods ---

    @Transactional
    public ReviewResponse createReview(ReviewRequest reviewRequest, Long userId) {
        // Check if user has already reviewed this attraction
        if (reviewRepository.existsByUserIdAndAttractionId(userId, reviewRequest.getAttractionId())) {
            throw new ConflictException("You have already reviewed this attraction.");
        }

        User user = userService.findUserEntityById(userId); // Get User entity
        Attraction attraction = attractionService.findAttractionEntityById(reviewRequest.getAttractionId()); // Get Attraction entity

        Review review = new Review();
        review.setUser(user);
        review.setAttraction(attraction);
        review.setRating(reviewRequest.getRating());
        review.setTitle(reviewRequest.getTitle());
        review.setComment(reviewRequest.getComment());
        review.setVisitDate(reviewRequest.getVisitDate());
        review.setImageUrl(reviewRequest.getImageUrl());
        review.setHelpfulCount(0); // Initial helpful count

        Review savedReview = reviewRepository.save(review);

        // After saving the review, update the attraction's average rating and count
        attractionService.updateAttractionRatingAndCount(attraction.getId());

        return convertToReviewResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        return convertToReviewResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByAttractionId(Long attractionId, Pageable pageable) {
        // Ensure attraction exists
        attractionService.findAttractionEntityById(attractionId); // Throws ResourceNotFoundException if not found

        Page<Review> reviewPage = reviewRepository.findByAttractionId(attractionId, pageable);
        return convertToReviewResponsePage(reviewPage);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByUserId(Long userId, Pageable pageable) {
        // Ensure user exists
        userService.findUserEntityById(userId); // Throws ResourceNotFoundException if not found

        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);
        return convertToReviewResponsePage(reviewPage);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest reviewRequest, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Authorization check: Only the author can update their review
        if (!review.getUser().getId().equals(currentUserId)) {
            // Or if the current user is not an ADMIN (add role check if needed)
            throw new ForbiddenException("You are not authorized to update this review.");
        }

        // AttractionId of a review generally shouldn't change.
        // If it needs to, validation for the new attractionId would be required.
        // For now, we assume attractionId remains the same.

        review.setRating(reviewRequest.getRating());
        review.setTitle(reviewRequest.getTitle());
        review.setComment(reviewRequest.getComment());
        review.setVisitDate(reviewRequest.getVisitDate());
        review.setImageUrl(reviewRequest.getImageUrl());
        // helpfulCount is not updated by the user

        Review updatedReview = reviewRepository.save(review);

        // After updating the review, update the attraction's average rating and count
        attractionService.updateAttractionRatingAndCount(review.getAttraction().getId());

        return convertToReviewResponse(updatedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        Long attractionIdToUpdate = review.getAttraction().getId();

        // Authorization check: Only the author or an admin can delete a review
        // For simplicity, only author check here. Add admin role check if needed.
        User currentUser = userService.getCurrentAuthenticatedUser(); // Assuming this method exists and is reliable
        boolean isAdmin = currentUser != null && currentUser.getRoles().contains("ADMIN"); // Adjust role string as needed

        if (!review.getUser().getId().equals(currentUserId) && !isAdmin) {
            throw new ForbiddenException("You are not authorized to delete this review.");
        }

        reviewRepository.delete(review);

        // After deleting the review, update the attraction's average rating and count
        attractionService.updateAttractionRatingAndCount(attractionIdToUpdate);
    }

    // Method to increment helpful count (example)
    @Transactional
    public ReviewResponse incrementHelpfulCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        Review updatedReview = reviewRepository.save(review);
        return convertToReviewResponse(updatedReview);
    }
}
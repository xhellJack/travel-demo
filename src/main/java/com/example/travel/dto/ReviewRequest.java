package com.example.travel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull(message = "Attraction ID cannot be null when creating a review")
    private Long attractionId; // 创建评价时，必须指定是对哪个景点的评价

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 5000, message = "Comment must be less than 5000 characters")
    private String comment;

    @PastOrPresent(message = "Visit date must be in the past or present")
    private LocalDate visitDate;

    @Size(max = 500, message = "Image URL must be less than 500 characters")
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    // Note: helpfulCount is usually managed by the system, not set by the user during creation/update.
    // For updates, the review ID would typically be part of the URL path,
    // and attractionId is generally not updatable for an existing review.
    // If this DTO is also used for updates, attractionId might be ignored or validated.
}
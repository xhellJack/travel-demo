package com.example.travel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "attraction_id"}, name = "uk_user_attraction_review")
}) // Added unique constraint: one user can review an attraction only once
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;

    @Column(nullable = false)
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating; // e.g., 1-5 stars

    @Column(length = 255)
    @Size(max = 255, message = "Review title must be less than 255 characters")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Size(max = 5000, message = "Review comment must be less than 5000 characters")
    private String comment;

    @Column(name = "visit_date")
    @PastOrPresent(message = "Visit date must be in the past or present")
    private LocalDate visitDate;

    @Column(name = "image_url", length = 500)
    @Size(max = 500)
    // Consider @URL validation
    private String imageUrl; // URL for a single image related to the review

    @Column(name = "helpful_count")
    @ColumnDefault("0")
    @Min(0)
    private Integer helpfulCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (helpfulCount == null) {
            helpfulCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
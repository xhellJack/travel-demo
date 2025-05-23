package com.example.travel.repository;

import com.example.travel.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// ... other imports ...

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // ... any existing methods ...

    // For calculating average rating
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.attraction.id = :attractionId")
    Double calculateAverageRatingByAttractionId(@Param("attractionId") Long attractionId);

    // For counting reviews
    @Query("SELECT COUNT(r) FROM Review r WHERE r.attraction.id = :attractionId")
    Integer countReviewsByAttractionId(@Param("attractionId") Long attractionId);

    Page<Review> findByAttractionId(Long attractionId, Pageable pageable); // For paginated reviews by attraction
    Page<Review> findByUserId(Long userId, Pageable pageable); // For paginated reviews by user

    boolean existsByUserIdAndAttractionId(Long userId, Long attractionId); // To check if user already reviewed
}
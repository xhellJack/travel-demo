package com.example.travel.repository;

import com.example.travel.entity.UserFavorite;
import com.example.travel.entity.UserFavoriteId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // If using custom delete query
import org.springframework.data.jpa.repository.Query; // If using custom delete query
import org.springframework.data.repository.query.Param; // If using custom delete query
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UserFavoriteId> {

    boolean existsByUser_IdAndAttraction_Id(Long userId, Long attractionId);

    // Method to find all favorites for a user, with pagination
    Page<UserFavorite> findByUser_Id(Long userId, Pageable pageable);

    // Custom delete method (alternative to deleteById if you don't want to fetch first)
    // This is more efficient if you just have the IDs.
    @Modifying // Indicates that this query will modify data
    void deleteByUser_IdAndAttraction_Id(Long userId, Long attractionId);

    // If you prefer a JPQL query for deletion:
    // @Modifying
    // @Query("DELETE FROM UserFavorite uf WHERE uf.user.id = :userId AND uf.attraction.id = :attractionId")
    // void deleteFavorite(@Param("userId") Long userId, @Param("attractionId") Long attractionId);
}
package com.example.travel.repository;

import com.example.travel.entity.Itinerary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // If you need dynamic queries later
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> /*, JpaSpecificationExecutor<Itinerary> */ {

    Page<Itinerary> findByUserId(Long userId, Pageable pageable);

    Page<Itinerary> findByIsPublicTrue(Pageable pageable);

    // --- Analytics Queries ---
    @Query("SELECT ia.attraction.name, COUNT(ia.attraction.id) FROM ItineraryAttraction ia GROUP BY ia.attraction.name ORDER BY COUNT(ia.attraction.id) DESC")
    Page<Object[]> findMostPopularAttractionsInItineraries(Pageable pageable);

    @Query("SELECT t.name, COUNT(i.id) FROM Itinerary i JOIN i.tags t GROUP BY t.name ORDER BY COUNT(i.id) DESC")
    Page<Object[]> findTopItineraryTagsUsage(Pageable pageable);
}
package com.example.travel.repository;

import com.example.travel.entity.ItineraryAttraction;
import com.example.travel.entity.ItineraryAttractionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItineraryAttractionRepository extends JpaRepository<ItineraryAttraction, ItineraryAttractionId> {

}
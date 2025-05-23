package com.example.travel.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // Important for composite keys
public class ItineraryAttractionId implements Serializable {
    private static final long serialVersionUID = 1L; // Good practice
    private Long itinerary; // Corresponds to Itinerary.id
    private Long attraction; // Corresponds to Attraction.id
}
package com.example.travel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "itinerary_attractions")
@IdClass(ItineraryAttractionId.class) // Composite key
public class ItineraryAttraction {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "itineraryAttractions", "tags"}) // Break cycles
    private Itinerary itinerary;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "favorites", "reviews", "tags", "itineraryAttractions"}) // Break cycles
    private Attraction attraction;

    @Column(name = "visit_date")
    // @NotNull // Depending on whether a specific date is always required for an item in itinerary
    private LocalDate visitDate; // Date to visit this specific attraction within the itinerary

    @Column(name = "order_in_itinerary")
    @Min(value = 0, message = "Order must be non-negative")
    private Integer orderInItinerary; // Sequence of this attraction in the itinerary

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;
    // Consider custom validation: startTime must be before endTime if both are present

    @Column(name = "custom_cost", precision = 10, scale = 2)
    @PositiveOrZero(message = "Custom cost must be non-negative")
    private BigDecimal customCost;

    @Column(name = "transportation_to_next_notes", length = 255)
    @Size(max = 255)
    private String transportationToNextNotes;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000)
    private String notes; // User's notes for this attraction in this itinerary
}
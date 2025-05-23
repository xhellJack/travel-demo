package com.example.travel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.URL;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryUpdateRequest {

    @Size(min = 2, max = 100, message = "Itinerary name must be between 2 and 100 characters")
    private String name;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;

    @FutureOrPresent(message = "Start date must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @FutureOrPresent(message = "End date must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; // Add validation: endDate >= startDate (typically class-level)

    private Boolean isPublic;

    @Size(max = 30, message = "Status must be less than 30 characters")
    private String status;

    @DecimalMin(value = "0.0", inclusive = true, message = "Total estimated cost must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Total estimated cost format is invalid")
    private BigDecimal totalEstimatedCost;

    @Size(max = 500, message = "Cover image URL must be less than 500 characters")
    @URL(message = "Cover image URL must be a valid URL")
    private String coverImageUrl;

    private Set<Long> tagIds;

    @Valid // Ensures validation of nested ItineraryAttractionRequest objects
    private List<ItineraryAttractionRequest> itineraryAttractions; // For update, service layer will handle replacing/modifying items
}
package com.example.travel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
// Consider adding a class-level validator for startDate before endDate
public class ItineraryCreateRequest {

    @NotBlank(message = "Itinerary name cannot be blank")
    @Size(min = 2, max = 100, message = "Itinerary name must be between 2 and 100 characters")
    private String name;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;

    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @FutureOrPresent(message = "End date must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; // Add validation: endDate >= startDate (typically class-level)

    private Boolean isPublic = false; // Default to private

    @Size(max = 30, message = "Status must be less than 30 characters")
    private String status = "PLANNING"; // Default status

    @DecimalMin(value = "0.0", inclusive = true, message = "Total estimated cost must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Total estimated cost format is invalid")
    private BigDecimal totalEstimatedCost;

    @Size(max = 500, message = "Cover image URL must be less than 500 characters")
    @URL(message = "Cover image URL must be a valid URL")
    private String coverImageUrl;

    private Set<Long> tagIds; // IDs of tags to associate

    @Valid // Ensures validation of nested ItineraryAttractionRequest objects
    private List<ItineraryAttractionRequest> itineraryAttractions;
}
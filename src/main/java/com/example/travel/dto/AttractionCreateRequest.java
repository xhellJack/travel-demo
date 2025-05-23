package com.example.travel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.URL; // If you want specific URL validation

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionCreateRequest {

    @NotBlank(message = "Attraction name cannot be blank")
    @Size(min = 2, max = 100, message = "Attraction name must be between 2 and 100 characters")
    private String name;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;

    @Size(max = 255, message = "Location must be less than 255 characters")
    private String location;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    @Size(max = 255, message = "Opening hours must be less than 255 characters")
    private String openingHours;

    @DecimalMin(value = "0.0", inclusive = true, message = "Ticket price must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Ticket price format is invalid (e.g., 12345678.99)")
    private BigDecimal ticketPrice;

    @Size(max = 500, message = "Image URL must be less than 500 characters")
    @URL(message = "Image URL must be a valid URL") // Basic URL validation
    private String imageUrl;

    // Latitude and Longitude validation can be tricky, often just type check or range if strict
    private Double latitude; // e.g., between -90 and 90
    private Double longitude; // e.g., between -180 and 180

    @NotBlank(message = "Category cannot be blank")
    @Size(max = 50, message = "Category must be less than 50 characters")
    private String category;

    @Size(max = 30, message = "Contact phone must be less than 30 characters")
    // You might use @Pattern for specific phone number formats if needed
    private String contactPhone;

    @Size(max = 255, message = "Website URL must be less than 255 characters")
    @URL(message = "Website URL must be a valid URL")
    private String website;

    @DecimalMin(value = "0.0", inclusive = false, message = "Estimated duration must be positive")
    @Digits(integer = 2, fraction = 2, message = "Estimated duration format is invalid (e.g., 2.5 for 2.5 hours)")
    private BigDecimal estimatedDurationHours;

    @Size(max = 100, message = "Best time to visit must be less than 100 characters")
    private String bestTimeToVisit;

    @Size(max = 30, message = "Status must be less than 30 characters")
    private String status; // e.g., OPEN, CLOSED

    private Set<Long> tagIds; // IDs of tags to associate with this attraction
}
package com.example.travel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionUpdateRequest {

    @Size(min = 2, max = 100, message = "Attraction name must be between 2 and 100 characters")
    private String name; // All fields are optional for update

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;

    @Size(max = 255, message = "Location must be less than 255 characters")
    private String location;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    @Size(max = 255, message = "Opening hours must be less than 255 characters")
    private String openingHours;

    @DecimalMin(value = "0.0", inclusive = true, message = "Ticket price must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Ticket price format is invalid")
    private BigDecimal ticketPrice;

    @Size(max = 500, message = "Image URL must be less than 500 characters")
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    private Double latitude;
    private Double longitude;

    @Size(max = 50, message = "Category must be less than 50 characters")
    private String category;

    @Size(max = 30, message = "Contact phone must be less than 30 characters")
    private String contactPhone;

    @Size(max = 255, message = "Website URL must be less than 255 characters")
    @URL(message = "Website URL must be a valid URL")
    private String website;

    @DecimalMin(value = "0.0", inclusive = false, message = "Estimated duration must be positive")
    @Digits(integer = 2, fraction = 2, message = "Estimated duration format is invalid")
    private BigDecimal estimatedDurationHours;

    @Size(max = 100, message = "Best time to visit must be less than 100 characters")
    private String bestTimeToVisit;

    @Size(max = 30, message = "Status must be less than 30 characters")
    private String status;

    private Set<Long> tagIds; // Allows updating the set of associated tags
}
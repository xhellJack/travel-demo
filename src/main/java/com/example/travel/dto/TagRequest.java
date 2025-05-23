package com.example.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagRequest {

    @NotBlank(message = "Tag name cannot be blank")
    @Size(min = 1, max = 50, message = "Tag name must be between 1 and 50 characters")
    private String name;

    @Size(max = 50, message = "Tag category must be less than 50 characters")
    private String tagCategory; // e.g., "Interest", "Activity", "Cuisine"

    @Size(max = 500, message = "Tag description must be less than 500 characters")
    private String description;
}
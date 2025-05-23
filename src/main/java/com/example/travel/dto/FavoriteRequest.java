package com.example.travel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequest {

    @NotNull(message = "Attraction ID cannot be null")
    private Long attractionId;

}
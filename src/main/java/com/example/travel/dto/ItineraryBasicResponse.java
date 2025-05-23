package com.example.travel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryBasicResponse {
    private Long id;
    private String name;
    private String coverImageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Boolean isPublic;
    private String status;
    private UserBasicResponse user; // 行程的创建者
    // Potentially add:
    // private Integer numberOfDays;
    // private Integer numberOfAttractions;
}
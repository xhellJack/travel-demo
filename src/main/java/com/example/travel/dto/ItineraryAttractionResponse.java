package com.example.travel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryAttractionResponse {
    // No 'id' for ItineraryAttraction itself in response, usually identified by itineraryId + attractionId + order/date
    private AttractionBasicResponse attraction; // 景点基本信息

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    private Integer orderInItinerary;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private BigDecimal customCost;
    private String transportationToNextNotes;
    private String notes;
}
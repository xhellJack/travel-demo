package com.example.travel.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryAttractionRequest {

    @NotNull(message = "Attraction ID cannot be null")
    private Long attractionId; // 关联的景点ID

    @FutureOrPresent(message = "Visit date must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate; // 计划访问日期

    @PositiveOrZero(message = "Order in itinerary must be a non-negative number")
    private Integer orderInItinerary; // 在行程中的顺序

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime; // 计划开始时间

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime; // 计划结束时间

    @DecimalMin(value = "0.0", inclusive = true, message = "Custom cost must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Custom cost format is invalid")
    private BigDecimal customCost; // 此景点的自定义花费

    @Size(max = 255, message = "Transportation notes must be less than 255 characters")
    private String transportationToNextNotes; // 前往下一个景点的交通备注

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes; // 关于此行程景点的用户备注
}
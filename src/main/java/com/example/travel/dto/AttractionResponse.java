package com.example.travel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionResponse {
    private Long id;
    private String name;
    private String description;
    private String location; // 概要位置，例如 "东京塔附近"
    private String address; // 详细地址
    private String openingHours;
    private BigDecimal ticketPrice;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String category; // 例如 "Historic Site", "Museum", "Park"
    private Double averageRating;
    private Integer ratingCount;
    private String contactPhone;
    private String website;
    private BigDecimal estimatedDurationHours; // 例如 2.5 表示 2.5小时
    private String bestTimeToVisit; // 例如 "Spring", "Morning"
    private String status; // 例如 "OPEN", "CLOSED_TEMPORARILY", "UNDER_CONSTRUCTION"

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Set<TagResponse> tags; // 嵌套TagResponse
}
package com.example.travel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryResponse {
    private Long id;
    private String name;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Boolean isPublic;
    private String status; // e.g., "PLANNING", "ACTIVE", "COMPLETED", "CANCELLED"
    private BigDecimal totalEstimatedCost;
    private String coverImageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private UserBasicResponse user; // 行程的创建者
    private Set<TagResponse> tags; // 行程关联的标签
    private List<ItineraryAttractionResponse> itineraryAttractions; // 行程中包含的景点安排
}
package com.example.travel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Integer rating;
    private String title;
    private String comment;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    private String imageUrl;
    private Integer helpfulCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private UserBasicResponse user; // 评价的作者 (简化信息)
    private AttractionBasicResponse attraction; // 被评价的景点 (简化信息)
}
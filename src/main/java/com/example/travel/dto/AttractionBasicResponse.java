package com.example.travel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionBasicResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private String category;
    private String location; // 概要位置
    private Double averageRating;
}
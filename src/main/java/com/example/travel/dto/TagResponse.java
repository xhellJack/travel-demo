package com.example.travel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    private Long id;
    private String name;
    private String tagCategory;
    private String description;
}
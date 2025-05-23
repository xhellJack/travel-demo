package com.example.travel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicResponse {
    private Long id;
    private String username;
    private String avatar; // Maybe just username and ID for some contexts
}
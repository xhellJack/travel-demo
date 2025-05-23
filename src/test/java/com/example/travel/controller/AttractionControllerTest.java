package com.example.travel.controller;

import com.example.travel.dto.AttractionCreateRequest;
import com.example.travel.dto.AttractionResponse;
import com.example.travel.dto.TagResponse;
import com.example.travel.service.AttractionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // For testing secured endpoints
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import org.springframework.security.test.context.support.WithMockUser;

// Import your SecurityConfig and other necessary configs for the test context
// For example, if JwtTokenUtil and UserDetailsService are needed for security setup
import com.example.travel.config.SecurityConfig;
import com.example.travel.config.JwtTokenUtil; // If needed for token filter bean in SecurityConfig
import com.example.travel.service.UserService; // If UserDetailsService is UserService

@WebMvcTest(AttractionController.class) // Test only the AttractionController layer
@Import({SecurityConfig.class, JwtTokenUtil.class}) // Import security config for @PreAuthorize
        // Also import JwtTokenUtil if it's a bean used in SecurityConfig
class AttractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Mocks the AttractionService in the Spring context
    private AttractionService attractionService;

    @MockBean // Mock UserService if your SecurityConfig or controller methods depend on it
    private UserService userService; // (e.g., for @PreAuthorize("@userService.isSelf(...)"))

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON strings

    private AttractionResponse attractionResponse1;
    private AttractionCreateRequest attractionCreateRequest;

    @BeforeEach
    void setUp() {
        Set<TagResponse> tags = new HashSet<>();
        tags.add(new TagResponse(1L, "历史", "兴趣点", "历史相关"));

        attractionResponse1 = new AttractionResponse(
                1L, "故宫", "故宫描述", "北京", "详细地址", "08:00-17:00",
                new BigDecimal("60.00"), "image.jpg", 39.9, 116.3, "历史遗迹",
                4.8, 1500, "12345", "website.com", new BigDecimal("4.0"),
                "春秋", "OPEN", LocalDateTime.now(), LocalDateTime.now(), tags
        );

        attractionCreateRequest = new AttractionCreateRequest(
                "长城", "长城描述", "北京北", "详细地址", "全天",
                new BigDecimal("100.00"), "wall.jpg", 40.4, 116.0, "历史遗迹",
                "010-xxxx", "greatwall.cn", new BigDecimal("6.0"),
                "四季", "OPEN", Collections.singleton(1L) // tagId
        );
    }

    @Test
    @WithMockUser // For endpoints that require authentication but not specific roles
    void getAttractionById_whenAttractionExists_shouldReturnAttraction() throws Exception {
        given(attractionService.getAttractionById(1L)).willReturn(attractionResponse1);

        mockMvc.perform(get("/api/attractions/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("故宫"));
    }

    // Test for ResourceNotFoundException scenario would also be good

    @Test
    @WithMockUser(roles = "ADMIN") // For endpoints requiring ADMIN role
    void createAttraction_whenAdmin_shouldCreateAttraction() throws Exception {
        given(attractionService.createAttraction(any(AttractionCreateRequest.class))).willReturn(attractionResponse1); // Assume it returns something similar

        mockMvc.perform(post("/api/attractions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attractionCreateRequest)))
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(attractionResponse1.getName())); // Adjust based on what create returns
    }

    @Test
    @WithMockUser(roles = "USER") // Test access denied for non-admin
    void createAttraction_whenUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/attractions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attractionCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchAttractions_shouldReturnPageOfAttractions() throws Exception {
        Page<AttractionResponse> attractionPage = new PageImpl<>(List.of(attractionResponse1), PageRequest.of(0, 1), 1);
        given(attractionService.searchAttractions(any(), any(), any(), any(), any(Pageable.class)))
                .willReturn(attractionPage);

        mockMvc.perform(get("/api/attractions")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("故宫"));
    }

    // Add more tests for update, delete, popular, validation failures, etc.
}
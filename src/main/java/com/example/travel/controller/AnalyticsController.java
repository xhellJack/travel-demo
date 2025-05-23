package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.analytics.CountByCriteriaResponse;
import com.example.travel.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN')") // 假设所有分析接口都仅限管理员访问
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // --- 用户分析API ---

    /**
     * 获取按性别统计的用户数量。
     */
    @GetMapping("/users/gender-distribution")
    public ResponseEntity<Result<List<CountByCriteriaResponse>>> getUserGenderDistribution() {
        List<CountByCriteriaResponse> distribution = analyticsService.getUserCountByGender();
        return ResponseEntity.ok(Result.success(distribution));
    }

    /**
     * 获取最受欢迎的用户偏好标签（分页）。
     * @param page 页码 (0-indexed)
     * @param size 每页数量
     */
    @GetMapping("/users/top-preferred-tags")
    public ResponseEntity<Result<Page<CountByCriteriaResponse>>> getTopUserPreferredTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<CountByCriteriaResponse> topTags = analyticsService.getTopUserPreferredTags(page, size);
        return ResponseEntity.ok(Result.success(topTags));
    }

    // --- 景点分析API ---

    /**
     * 获取按类别统计的景点数量。
     */
    @GetMapping("/attractions/category-distribution")
    public ResponseEntity<Result<List<CountByCriteriaResponse>>> getAttractionCategoryDistribution() {
        List<CountByCriteriaResponse> distribution = analyticsService.getAttractionCountByCategory();
        return ResponseEntity.ok(Result.success(distribution));
    }

    /**
     * 获取使用频率最高的景点标签（分页）。
     * @param page 页码 (0-indexed)
     * @param size 每页数量
     */
    @GetMapping("/attractions/top-tags-usage")
    public ResponseEntity<Result<Page<CountByCriteriaResponse>>> getTopAttractionTagsUsage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<CountByCriteriaResponse> topUsage = analyticsService.getTopAttractionTagsUsage(page, size);
        return ResponseEntity.ok(Result.success(topUsage));
    }

    // --- 行程分析API ---

    /**
     * 获取行程中最常包含的景点（分页）。
     * @param page 页码 (0-indexed)
     * @param size 每页数量
     */
    @GetMapping("/itineraries/popular-attractions")
    public ResponseEntity<Result<Page<CountByCriteriaResponse>>> getMostPopularAttractionsInItineraries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<CountByCriteriaResponse> popularAttractions = analyticsService.getMostPopularAttractionsInItineraries(page, size);
        return ResponseEntity.ok(Result.success(popularAttractions));
    }

    /**
     * 获取使用频率最高的行程标签（分页）。
     * @param page 页码 (0-indexed)
     * @param size 每页数量
     */
    @GetMapping("/itineraries/top-tags-usage")
    public ResponseEntity<Result<Page<CountByCriteriaResponse>>> getTopItineraryTagsUsage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<CountByCriteriaResponse> topUsage = analyticsService.getTopItineraryTagsUsage(page, size);
        return ResponseEntity.ok(Result.success(topUsage));
    }

    // 您可以根据 AnalyticsService 中添加的新分析方法，继续在这里添加对应的API端点。
    // 例如：
    // @GetMapping("/attractions/top-rated")
    // public ResponseEntity<Result<Page<AttractionResponse>>> getTopRatedAttractions(
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "5") int size) {
    //     // 调用 AttractionService 或 AnalyticsService 中的方法
    // }
}
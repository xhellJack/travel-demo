package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.AttractionResponse;
import com.example.travel.entity.User;
import com.example.travel.service.RecommendationService;
import com.example.travel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService, UserService userService) {
        this.recommendationService = recommendationService;
        this.userService = userService;
    }

    /**
     * 获取基于当前登录用户偏好的景点推荐 (分页)
     */
    @GetMapping("/user-preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<Page<AttractionResponse>>> getPersonalizedRecommendations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Page<AttractionResponse> recommendations = recommendationService.getRecommendationsBasedOnUserPreferences(currentUser.getId(), page, size);
        return ResponseEntity.ok(Result.success(recommendations));
    }

    /**
     * 获取热门景点推荐 (分页) - 公开访问
     */
    @GetMapping("/popular-attractions")
    public ResponseEntity<Result<Page<AttractionResponse>>> getPopularAttractions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AttractionResponse> popular = recommendationService.getPopularAttractionRecommendations(page, size);
        return ResponseEntity.ok(Result.success(popular));
    }

    // 未来可以添加更多推荐API端点，例如：
    // - 为特定景点推荐相似景点
    // - 为特定行程推荐补充景点

    @GetMapping("/item-based-cf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<List<AttractionResponse>>> getItemBasedCfRecommendations(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int numRecs) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<AttractionResponse> recommendations = recommendationService.getItemBasedCfRecommendations(
                currentUser.getId(), numRecs
        );
        return ResponseEntity.ok(Result.success(recommendations));
    }
}
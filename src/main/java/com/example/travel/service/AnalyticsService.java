package com.example.travel.service;

import com.example.travel.dto.analytics.CountByCriteriaResponse;
import com.example.travel.repository.AttractionRepository;
import com.example.travel.repository.ItineraryRepository;
import com.example.travel.repository.ReviewRepository;
import com.example.travel.repository.UserRepository;
// TagRepository might also be needed if you add analytics related to all tags in the system
// import com.example.travel.repository.TagRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) // Most analytics methods will be read-only
public class AnalyticsService {

    private final UserRepository userRepository;
    private final AttractionRepository attractionRepository;
    private final ItineraryRepository itineraryRepository;
    // private final ReviewRepository reviewRepository; // Uncomment if you add review-specific analytics
    // private final TagRepository tagRepository; // Uncomment if you add general tag analytics

    @Autowired
    public AnalyticsService(UserRepository userRepository,
                            AttractionRepository attractionRepository,
                            ItineraryRepository itineraryRepository
            /* ReviewRepository reviewRepository, TagRepository tagRepository */) {
        this.userRepository = userRepository;
        this.attractionRepository = attractionRepository;
        this.itineraryRepository = itineraryRepository;
        // this.reviewRepository = reviewRepository;
        // this.tagRepository = tagRepository;
    }

    // --- 用户分析 (User Analytics) ---

    /**
     * 获取按性别统计的用户数量。
     * @return List of CountByCriteriaResponse where criteria is gender and count is the number of users.
     */
    public List<CountByCriteriaResponse> getUserCountByGender() {
        List<Object[]> results = userRepository.countUsersByGender();
        return results.stream()
                .map(result -> new CountByCriteriaResponse(
                        result[0] != null ? result[0].toString() : "UNKNOWN", // Criteria (Gender)
                        (Long) result[1]                                     // Count
                ))
                .collect(Collectors.toList());
    }

    /**
     * 获取最受欢迎的用户偏好标签（分页）。
     * @param page Page number (0-indexed).
     * @param size Number of items per page.
     * @return Page of CountByCriteriaResponse where criteria is tag name and count is number of users.
     */
    public Page<CountByCriteriaResponse> getTopUserPreferredTags(int page, int size) {
        // The query in UserRepository already orders by count DESC
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = userRepository.findTopUserPreferredTags(pageable);
        return results.map(result -> new CountByCriteriaResponse(
                (String) result[0], // Criteria (Tag Name)
                (Long) result[1]    // Count
        ));
    }

    // --- 景点分析 (Attraction Analytics) ---

    /**
     * 获取按类别统计的景点数量。
     * @return List of CountByCriteriaResponse where criteria is category and count is number of attractions.
     */
    public List<CountByCriteriaResponse> getAttractionCountByCategory() {
        List<Object[]> results = attractionRepository.countAttractionsByCategory();
        return results.stream()
                .map(result -> new CountByCriteriaResponse(
                        result[0] != null ? result[0].toString() : "UNCATEGORIZED", // Criteria (Category)
                        (Long) result[1]                                            // Count
                ))
                .collect(Collectors.toList());
    }

    /**
     * 获取使用频率最高的景点标签（分页）。
     * @param page Page number (0-indexed).
     * @param size Number of items per page.
     * @return Page of CountByCriteriaResponse where criteria is tag name and count is number of attractions.
     */
    public Page<CountByCriteriaResponse> getTopAttractionTagsUsage(int page, int size) {
        // The query in AttractionRepository already orders by count DESC
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = attractionRepository.findTopAttractionTagsUsage(pageable);
        return results.map(result -> new CountByCriteriaResponse(
                (String) result[0], // Criteria (Tag Name)
                (Long) result[1]    // Count
        ));
    }

    // --- 行程分析 (Itinerary Analytics) ---

    /**
     * 获取行程中最常包含的景点（分页）。
     * @param page Page number (0-indexed).
     * @param size Number of items per page.
     * @return Page of CountByCriteriaResponse where criteria is attraction name and count is occurrence in itineraries.
     */
    public Page<CountByCriteriaResponse> getMostPopularAttractionsInItineraries(int page, int size) {
        // The query in ItineraryRepository already orders by count DESC
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = itineraryRepository.findMostPopularAttractionsInItineraries(pageable);
        return results.map(result -> new CountByCriteriaResponse(
                (String) result[0], // Criteria (Attraction Name)
                (Long) result[1]    // Count
        ));
    }

    /**
     * 获取使用频率最高的行程标签（分页）。
     * @param page Page number (0-indexed).
     * @param size Number of items per page.
     * @return Page of CountByCriteriaResponse where criteria is tag name and count is number of itineraries.
     */
    public Page<CountByCriteriaResponse> getTopItineraryTagsUsage(int page, int size) {
        // The query in ItineraryRepository already orders by count DESC
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = itineraryRepository.findTopItineraryTagsUsage(pageable);
        return results.map(result -> new CountByCriteriaResponse(
                (String) result[0], // Criteria (Tag Name)
                (Long) result[1]    // Count
        ));
    }


    // - 获取评分最高的Top N景点 (可以直接调用AttractionService的方法，并转换为通用分析DTO，或在这里直接调用AttractionRepository)
    // - 用户注册趋势 (需要用户表有注册时间，并且按时间段分组)
    // - 等等...
}
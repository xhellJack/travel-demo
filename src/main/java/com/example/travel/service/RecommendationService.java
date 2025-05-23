package com.example.travel.service;

import com.example.travel.dto.AttractionResponse; // 使用我们已有的DTO
import com.example.travel.entity.*;
import com.example.travel.repository.AttractionRepository;
import com.example.travel.repository.ReviewRepository;
import com.example.travel.repository.UserFavoriteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final UserService userService;
    private final AttractionService attractionService; // 用于DTO转换
    private final AttractionRepository attractionRepository;

    private final ReviewRepository reviewRepository;
    private final UserFavoriteRepository userFavoriteRepository;

    // 内存数据结构: attractionId -> {userId -> rating/preference_score}
// 用于快速查找哪些用户与特定景点有交互及其偏好程度
    private Map<Long, Map<Long, Double>> attractionUserPreferences;

    // 内存数据结构: userId -> {attractionId -> rating/preference_score}
// 用于快速查找特定用户喜欢哪些景点及其偏好程度
    private Map<Long, Map<Long, Double>> userAttractionPreferences;
    @Autowired
    public RecommendationService(UserService userService,
                                 AttractionService attractionService,
                                 AttractionRepository attractionRepository,
                                 ReviewRepository reviewRepository, // Add
                                 UserFavoriteRepository userFavoriteRepository) { // Add
        this.userService = userService;
        this.attractionService = attractionService;
        this.attractionRepository = attractionRepository;
        this.reviewRepository = reviewRepository; // Add
        this.userFavoriteRepository = userFavoriteRepository; // Add
        this.attractionUserPreferences = new ConcurrentHashMap<>();
        this.userAttractionPreferences = new ConcurrentHashMap<>();
    }


    /**
     * 基于用户偏好标签的内容推荐 (简化版)
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页的景点推荐
     */
    public Page<AttractionResponse> getRecommendationsBasedOnUserPreferences(Long userId, int page, int size) {
        User user = userService.findUserEntityById(userId);
        Set<Tag> preferredTags = user.getPreferredTags();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "averageRating", "ratingCount")); // Define pageable here

        if (CollectionUtils.isEmpty(preferredTags)) {
            return attractionService.getPopularAttractions(pageable);
        }

        Set<Long> preferredTagIds = preferredTags.stream().map(Tag::getId).collect(Collectors.toSet());

        Page<Attraction> recommendedAttractionsPage = attractionRepository.findRecommendationsByUserPreferences(
                userId, preferredTagIds, pageable
        );

        return recommendedAttractionsPage.map(attractionService::convertToAttractionResponse);
    }

    /**
     * 获取热门景点推荐 (直接复用 AttractionService 的逻辑)
     * @param page 页码
     * @param size 每页数量
     * @return 分页的热门景点
     */
    public Page<AttractionResponse> getPopularAttractionRecommendations(int page, int size) {
        // AttractionService.getPopularAttractions(limit) 返回 List，我们需要 Page
        // 因此，我们直接调用AttractionRepository的方法，或者让AttractionService提供一个返回Page的方法
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "averageRating").and(Sort.by(Sort.Direction.DESC, "ratingCount")));
        Page<Attraction> popularAttractions = attractionRepository.findByOrderByAverageRatingDescRatingCountDesc(pageable);
        return popularAttractions.map(attractionService::convertToAttractionResponse);
    }

    @PostConstruct
    public void initializeRecommendationData() {
        loadInteractionData();
        // 考虑异步执行或定时执行，如果景点数量很多
        // For now, let's call it directly for simplicity if data is small
        if (this.attractionUserPreferences != null && !this.attractionUserPreferences.isEmpty()) {
            System.out.println("RecommendationService: Proceeding to calculate attraction similarity matrix.");
            calculateAttractionSimilarityMatrix();
        } else {
            System.out.println("RecommendationService: No interaction data loaded, skipping similarity matrix calculation.");
            this.attractionSimilarityMatrix = new ConcurrentHashMap<>(); //确保即使跳过也有初始化的空map
        }
    }

    @PostConstruct // 应用启动后执行数据加载
    @Transactional(readOnly = true) // 确保在事务中加载，特别是对于懒加载的实体
    public void loadInteractionData() {
        Map<Long, Map<Long, Double>> tempAttractionPrefs = new HashMap<>();
        Map<Long, Map<Long, Double>> tempUserPrefs = new HashMap<>();

        // 1. 加载评分数据
        List<Review> allReviews = reviewRepository.findAll();
        for (Review review : allReviews) {
            if (review.getUser() != null && review.getAttraction() != null && review.getRating() != null) {
                Long userId = review.getUser().getId();
                Long attractionId = review.getAttraction().getId();
                double rating = review.getRating().doubleValue();

                tempAttractionPrefs.computeIfAbsent(attractionId, k -> new HashMap<>()).put(userId, rating);
                tempUserPrefs.computeIfAbsent(userId, k -> new HashMap<>()).put(attractionId, rating);
            }
        }

        // 2. 加载收藏数据 (可以赋予一个固定高分，例如 5.0，如果评分范围是1-5)
        // 注意：如果一个用户既收藏又评分了同一个景点，需要决定如何合并分数（例如取最高值）
        final double favoriteScore = 5.0; // 假设收藏行为等同于5分评价
        List<UserFavorite> allFavorites = userFavoriteRepository.findAll();
        for (UserFavorite favorite : allFavorites) {
            if (favorite.getUser() != null && favorite.getAttraction() != null) {
                Long userId = favorite.getUser().getId();
                Long attractionId = favorite.getAttraction().getId();

                // 更新景点用户偏好
                Map<Long, Double> usersWhoRatedAttraction = tempAttractionPrefs.computeIfAbsent(attractionId, k -> new HashMap<>());
                usersWhoRatedAttraction.put(userId, Math.max(usersWhoRatedAttraction.getOrDefault(userId, 0.0), favoriteScore));

                // 更新用户景点偏好
                Map<Long, Double> attractionsRatedByUser = tempUserPrefs.computeIfAbsent(userId, k -> new HashMap<>());
                attractionsRatedByUser.put(attractionId, Math.max(attractionsRatedByUser.getOrDefault(attractionId, 0.0), favoriteScore));
            }
        }

        // 3. (可选) 加载行程包含数据 (来自 ItineraryAttraction 表)
        // 这也可以被视为一种强烈的积极信号，可以赋予一个高分。
        // List<ItineraryAttraction> allItineraryAttractions = itineraryAttractionRepository.findAll(); // 需要注入 ItineraryAttractionRepository
        // for (ItineraryAttraction ia : allItineraryAttractions) {
        //     if (ia.getItinerary() != null && ia.getItinerary().getUser() != null && ia.getAttraction() != null) {
        //         Long userId = ia.getItinerary().getUser().getId();
        //         Long attractionId = ia.getAttraction().getId();
        //         double itineraryInclusionScore = 5.0; // 例如，与收藏同等重要或更高
        //
        //         Map<Long, Double> usersWhoInteractedWithAttraction = tempAttractionPrefs.computeIfAbsent(attractionId, k -> new HashMap<>());
        //         usersWhoInteractedWithAttraction.put(userId, Math.max(usersWhoInteractedWithAttraction.getOrDefault(userId, 0.0), itineraryInclusionScore));
        //
        //         Map<Long, Double> attractionsInteractedByUser = tempUserPrefs.computeIfAbsent(userId, k -> new HashMap<>());
        //         attractionsInteractedByUser.put(attractionId, Math.max(attractionsInteractedByUser.getOrDefault(attractionId, 0.0), itineraryInclusionScore));
        //     }
        // }
        // System.out.println("RecommendationService: Loaded " + allItineraryAttractions.size() + " itinerary items.");

        this.attractionUserPreferences = new ConcurrentHashMap<>(tempAttractionPrefs);
        this.userAttractionPreferences = new ConcurrentHashMap<>(tempUserPrefs);
        long endTime = System.currentTimeMillis();
        System.out.println("RecommendationService: Interaction data loaded. Users: " + this.userAttractionPreferences.size() + ", Attractions with interactions: " + this.attractionUserPreferences.size());
    }

    private Map<Long, Map<Long, Double>> attractionSimilarityMatrix;
    public void calculateAttractionSimilarityMatrix() {
        Map<Long, Map<Long, Double>> tempSimilarityMatrix = new ConcurrentHashMap<>();
        List<Long> attractionIds = new ArrayList<>(this.attractionUserPreferences.keySet()); // 所有有过交互的景点

        for (int i = 0; i < attractionIds.size(); i++) {
            for (int j = i + 1; j < attractionIds.size(); j++) { //只计算上三角或下三角，因为是对称的
                Long attr1Id = attractionIds.get(i);
                Long attr2Id = attractionIds.get(j);

                // 获取评价过这两个景点的用户向量
                Map<Long, Double> usersForAttr1 = this.attractionUserPreferences.get(attr1Id);
                Map<Long, Double> usersForAttr2 = this.attractionUserPreferences.get(attr2Id);

                double similarity = calculateCosineSimilarityForItems(usersForAttr1, usersForAttr2);

                if (similarity > 0) { // 只存储有一定相似度的
                    tempSimilarityMatrix.computeIfAbsent(attr1Id, k -> new HashMap<>()).put(attr2Id, similarity);
                    tempSimilarityMatrix.computeIfAbsent(attr2Id, k -> new HashMap<>()).put(attr1Id, similarity); // 对称存储
                }
            }
        }
        this.attractionSimilarityMatrix = tempSimilarityMatrix;
        System.out.println("RecommendationService: Attraction similarity matrix calculated for " + this.attractionSimilarityMatrix.size() + " attractions.");
    }

    // 基于物品的余弦相似度计算 (用户是维度)
    private double calculateCosineSimilarityForItems(Map<Long, Double> usersWhoRatedItem1, Map<Long, Double> usersWhoRatedItem2) {
        if (usersWhoRatedItem1 == null || usersWhoRatedItem2 == null) return 0.0;

        Set<Long> commonUsers = new HashSet<>(usersWhoRatedItem1.keySet());
        commonUsers.retainAll(usersWhoRatedItem2.keySet());

        if (commonUsers.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normItem1 = 0.0;
        double normItem2 = 0.0;

        for (Long userId : commonUsers) {
            dotProduct += usersWhoRatedItem1.get(userId) * usersWhoRatedItem2.get(userId);
        }

        for (Double rating : usersWhoRatedItem1.values()) {
            normItem1 += Math.pow(rating, 2);
        }
        normItem1 = Math.sqrt(normItem1);

        for (Double rating : usersWhoRatedItem2.values()) {
            normItem2 += Math.pow(rating, 2);
        }
        normItem2 = Math.sqrt(normItem2);

        if (normItem1 == 0 || normItem2 == 0) {
            return 0.0;
        }

        return dotProduct / (normItem1 * normItem2);
    }

    // 获取与给定景点相似的景点列表
    private List<AttractionSimilarityScore> getSimilarAttractions(Long attractionId, int count) {
        if (this.attractionSimilarityMatrix == null || !this.attractionSimilarityMatrix.containsKey(attractionId)) {
            return Collections.emptyList();
        }

        Map<Long, Double> similarItems = this.attractionSimilarityMatrix.get(attractionId);
        if (similarItems == null || similarItems.isEmpty()) {
            return Collections.emptyList();
        }

        return this.attractionSimilarityMatrix.get(attractionId).entrySet().stream()
                .map(entry -> new AttractionSimilarityScore(entry.getKey(), entry.getValue()))
                .sorted((s1, s2) -> Double.compare(s2.getSimilarity(), s1.getSimilarity()))
                .limit(count)
                .collect(Collectors.toList());
    }

    // 辅助内部类
    private static class AttractionSimilarityScore {
        private Long attractionId;
        private double similarity;
        // constructor, getters
        public AttractionSimilarityScore(Long attractionId, double similarity) { this.attractionId = attractionId; this.similarity = similarity; }
        public Long getAttractionId() { return attractionId; }
        public double getSimilarity() { return similarity; }
    }

    public List<AttractionResponse> getItemBasedCfRecommendations(Long userId, int numRecommendations) {
        if (this.userAttractionPreferences == null || !this.userAttractionPreferences.containsKey(userId) || this.attractionSimilarityMatrix == null) {
            // 用户数据不足或相似度矩阵未计算，回退到热门推荐
            return getPopularAttractionRecommendations(0, numRecommendations).getContent();
        }

        Map<Long, Double> targetUserPreferences = this.userAttractionPreferences.get(userId);
        Map<Long, Double> recommendationScores = new HashMap<>();

        // 遍历用户喜欢过的物品
        for (Map.Entry<Long, Double> userPrefEntry : targetUserPreferences.entrySet()) {
            Long likedAttractionId = userPrefEntry.getKey();
            double userRatingForLikedAttraction = userPrefEntry.getValue();

            // 找到与该喜欢物品相似的其他物品
            List<AttractionSimilarityScore> similarAttractions = getSimilarAttractions(likedAttractionId, 2 * numRecommendations); // 获取稍多一些相似物品

            for (AttractionSimilarityScore simAttraction : similarAttractions) {
                Long recommendedAttractionId = simAttraction.getAttractionId();
                double similarity = simAttraction.getSimilarity();

                // 如果用户尚未与推荐物品互动过
                if (!targetUserPreferences.containsKey(recommendedAttractionId)) {
                    // 预测分数 = 用户对原物品的评分 * 原物品与推荐物品的相似度
                    // 可以使用更复杂的加权方式
                    // 计算预测评分：Σ (similarity_ij * rating_uj) / Σ (similarity_ij)
                    // 这里简化为：Σ (similarity_ij * rating_ui) -> rating_ui 是用户对当前物品i的评分
                    // 我们将使用 rating_ui (即 userRatingForLikedAttraction) * similarity_ij 作为分数贡献
                    // 并对同一个推荐候选物品累加来自不同“已喜欢物品”的贡献
                    double predictedScore = recommendationScores.getOrDefault(recommendedAttractionId, 0.0);
                    predictedScore += userRatingForLikedAttraction * similarity; // 累加分数
                    recommendationScores.put(recommendedAttractionId, predictedScore);
                }
            }

        }

        // 排序并获取Top-N推荐
        List<Long> recommendedAttractionIds = recommendationScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(numRecommendations)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (recommendedAttractionIds.isEmpty()) {
            // 如果没有基于CF的推荐，可以回退
            return getPopularAttractionRecommendations(0, numRecommendations).getContent();
        }

        List<Attraction> attractions = attractionRepository.findAllById(recommendedAttractionIds);
        Map<Long, Attraction> attractionMap = attractions.stream()
                .collect(Collectors.toMap(Attraction::getId, attr -> attr));

        return recommendedAttractionIds.stream()
                .map(attractionMap::get)
                .filter(Objects::nonNull)
                .map(attractionService::convertToAttractionResponse)
                .collect(Collectors.toList());
    }


}
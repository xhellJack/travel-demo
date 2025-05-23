package com.example.travel.repository;

import com.example.travel.entity.Attraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Add @Repository for clarity

import java.util.List;
import java.util.Set;

@Repository // Good practice to add @Repository, though optional for Spring Data JPA
public interface AttractionRepository extends
        JpaRepository<Attraction, Long>,
        JpaSpecificationExecutor<Attraction> {

    // 按名称模糊搜索 (不区分大小写)
    List<Attraction> findByNameContainingIgnoreCase(String keyword);

    // 按类别精确搜索 (不区分大小写)
    List<Attraction> findByCategoryIgnoreCase(String category); // New: if needed for simple category search

    // 按最低平均评分筛选
    List<Attraction> findByAverageRatingGreaterThanEqual(Double minRating); // Changed Float to Double

    // 按标签ID查找景点
    List<Attraction> findByTags_Id(Long tagId);

    // 按标签名称查找景点
    List<Attraction> findByTags_Name(String tagName);

    // 按多个标签ID查找景点 (返回包含任一指定标签的景点)
    // 如果需要包含所有指定标签，查询会更复杂，可能需要自定义@Query或Specification
    List<Attraction> findByTags_IdIn(List<Long> tagIds); // New: if needed for "any of these tags"

    // 查找评分最高且评价数量较多的前N个景点 (用于热门推荐)
    // 返回Page，Service层可以取 .getContent() 来获得List
    Page<Attraction> findByOrderByAverageRatingDescRatingCountDesc(Pageable pageable);

    // --- Analytics Queries ---
    @Query("SELECT a.category, COUNT(a.id) FROM Attraction a WHERE a.category IS NOT NULL GROUP BY a.category ORDER BY COUNT(a.id) DESC")
    List<Object[]> countAttractionsByCategory();

    @Query("SELECT t.name, COUNT(a.id) FROM Attraction a JOIN a.tags t GROUP BY t.name ORDER BY COUNT(a.id) DESC")
    Page<Object[]> findTopAttractionTagsUsage(Pageable pageable);


    @Query("SELECT a FROM Attraction a JOIN a.tags t WHERE t.id IN :tagIds " +
            "AND a.id NOT IN (SELECT r.attraction.id FROM Review r WHERE r.user.id = :userId) " +
            "AND a.id NOT IN (SELECT uf.attraction.id FROM UserFavorite uf WHERE uf.user.id = :userId) " +
            "ORDER BY a.averageRating DESC, a.ratingCount DESC")
    Page<Attraction> findRecommendationsByUserPreferences(
            @Param("userId") Long userId,
            @Param("tagIds") Set<Long> tagIds,
            Pageable pageable
    );
    /*
    @Query(value = """
        SELECT * FROM attractions
        WHERE ST_Distance_Sphere(POINT(longitude, latitude), POINT(:param_lon, :param_lat)) < :radiusMeters
        ORDER BY ST_Distance_Sphere(POINT(longitude, latitude), POINT(:param_lon, :param_lat)) ASC
        """, nativeQuery = true)
    List<Attraction> findNearbyAttractions(
            @Param("param_lon") Double longitude, // Use different names for @Param if they clash with entity fields
            @Param("param_lat") Double latitude,
            @Param("radiusMeters") Integer radiusMeters
    );
    */
}
package com.example.travel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_activity_logs")
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 关联的用户

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id") // 可以为空，因为某些活动可能不直接关联到特定景点
    private Attraction attraction; // 关联的景点（可选）

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType; // 活动类型，例如: "VIEW_ATTRACTION", "SEARCH_ATTRACTIONS", "VIEW_ITINERARY"

    @Lob // 对于可能较长的文本，使用 @Lob
    @Column(name = "activity_details", columnDefinition = "TEXT")
    private String activityDetails; // 活动详情，例如: 搜索关键词、查看的行程ID、停留时长等

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp; // 活动发生的时间戳

    // 构造函数，方便Service层创建实例
    public UserActivityLog(User user, Attraction attraction, String activityType, String activityDetails) {
        this.user = user;
        this.attraction = attraction;
        this.activityType = activityType;
        this.activityDetails = activityDetails;
        this.timestamp = LocalDateTime.now();
    }

    // 另一个构造函数，当活动不直接关联景点时
    public UserActivityLog(User user, String activityType, String activityDetails) {
        this.user = user;
        this.activityType = activityType;
        this.activityDetails = activityDetails;
        this.timestamp = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
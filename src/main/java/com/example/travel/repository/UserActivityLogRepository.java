package com.example.travel.repository;

import com.example.travel.entity.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 可选，如果需要复杂动态查询
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long>, JpaSpecificationExecutor<UserActivityLog> {

    // 根据用户ID查找活动日志 (分页)
    Page<UserActivityLog> findByUser_IdOrderByTimestampDesc(Long userId, Pageable pageable);

    // 根据活动类型查找活动日志 (分页)
    Page<UserActivityLog> findByActivityTypeOrderByTimestampDesc(String activityType, Pageable pageable);

    // 根据用户ID和活动类型查找活动日志 (分页)
    Page<UserActivityLog> findByUser_IdAndActivityTypeOrderByTimestampDesc(Long userId, String activityType, Pageable pageable);

    // 根据景点ID查找特定类型的活动日志 (例如，查看某个景点的所有 VIEW_ATTRACTION 活动)
    Page<UserActivityLog> findByAttraction_IdAndActivityTypeOrderByTimestampDesc(Long attractionId, String activityType, Pageable pageable);

    // 如果需要，可以添加更多自定义查询方法
}
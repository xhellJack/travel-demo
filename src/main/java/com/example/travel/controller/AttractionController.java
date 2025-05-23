package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.AttractionCreateRequest;
import com.example.travel.dto.AttractionResponse;
import com.example.travel.dto.AttractionUpdateRequest;
import com.example.travel.service.AttractionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController {

    private final AttractionService attractionService;

    @Autowired
    public AttractionController(AttractionService attractionService) {
        this.attractionService = attractionService;
    }

    // 创建新景点 (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<AttractionResponse>> createAttraction(
            @Valid @RequestBody AttractionCreateRequest createRequest) {
        AttractionResponse attractionResponse = attractionService.createAttraction(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(attractionResponse));
    }

    // 根据ID获取景点详情 (Public)
    @GetMapping("/{id}")
    public ResponseEntity<Result<AttractionResponse>> getAttractionById(@PathVariable Long id) {
        AttractionResponse attractionResponse = attractionService.getAttractionById(id);
        return ResponseEntity.ok(Result.success(attractionResponse));
    }

    // 更新景点信息 (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<AttractionResponse>> updateAttraction(
            @PathVariable Long id,
            @Valid @RequestBody AttractionUpdateRequest updateRequest) {
        AttractionResponse updatedAttraction = attractionService.updateAttraction(id, updateRequest);
        return ResponseEntity.ok(Result.success(updatedAttraction));
    }

    // 删除景点 (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteAttraction(@PathVariable Long id) {
        attractionService.deleteAttraction(id);
        return ResponseEntity.ok(Result.success(null)); // Or ResponseEntity.noContent().build();
    }

    // 多条件分页查询景点 (Public)
    @GetMapping
    public ResponseEntity<Result<Page<AttractionResponse>>> searchAttractions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Float minRating,
            @RequestParam(required = false) List<Long> tagIds, // Pass tag IDs for filtering
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) { // Default sort by id ascending

        // Handle sorting (example: "name,asc", "averageRating,desc")
        // More robust sorting would parse sort parameters carefully
        Sort.Direction direction = Sort.Direction.fromString(sort[1].equalsIgnoreCase("desc") ? "DESC" : "ASC");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<AttractionResponse> attractionsPage = attractionService.searchAttractions(
                keyword, category, minRating, tagIds, pageable
        );
        return ResponseEntity.ok(Result.success(attractionsPage));
    }

    // 获取热门景点 (Public)
    @GetMapping("/popular")
    public ResponseEntity<Result<Page<AttractionResponse>>> getPopularAttractions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "averageRating,desc") String[] sortParams) { // 重命名以避免与Sort类混淆

        Pageable pageable; // 声明变量

        try {
            if (sortParams.length == 2) {
                String sortField = sortParams[0];
                Sort.Direction direction = Sort.Direction.fromString(sortParams[1].toUpperCase());
                Sort sort = Sort.by(new Sort.Order(direction, sortField));
                // 为热门推荐可以固定或添加次要排序条件
                if (!sortField.equalsIgnoreCase("ratingCount")) { // 避免重复添加
                    sort = sort.and(Sort.by(Sort.Direction.DESC, "ratingCount"));
                }
                if (!sortField.equalsIgnoreCase("averageRating") && !sortField.equalsIgnoreCase("ratingCount")) { //确保核心排序在
                    sort = sort.and(Sort.by(Sort.Direction.DESC, "averageRating"));
                }
                pageable = PageRequest.of(page, size, sort);
            } else if (sortParams.length == 1 && !sortParams[0].isEmpty() && !sortParams[0].contains(",")) {
                // 如果只提供一个排序字段名，默认为降序，并添加次要排序
                String sortField = sortParams[0];
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortField)
                        .and(Sort.by(Sort.Direction.DESC, "averageRating"))
                        .and(Sort.by(Sort.Direction.DESC, "ratingCount")));
            }
            else {
                // 默认排序或格式错误的sort参数
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.DESC, "ratingCount")));
            }
        } catch (IllegalArgumentException e) {
            // 如果排序方向字符串无效，则使用默认排序
            System.err.println("Invalid sort direction provided, using default sort: " + e.getMessage());
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "averageRating")
                    .and(Sort.by(Sort.Direction.DESC, "ratingCount")));
        }

        Page<AttractionResponse> popularAttractionsPage = attractionService.getPopularAttractions(pageable);
        return ResponseEntity.ok(Result.success(popularAttractionsPage));
    }
}
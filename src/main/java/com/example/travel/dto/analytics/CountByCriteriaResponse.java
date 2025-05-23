package com.example.travel.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountByCriteriaResponse {
    private String criteria; // 代表分组的条件，例如：性别名、标签名、景点类别名
    private Long count;    // 对应的数量
}
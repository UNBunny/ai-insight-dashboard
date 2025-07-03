package com.example.demo.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightRequest {
    private String topic;
    private Integer maxResults;
    private String language;
}

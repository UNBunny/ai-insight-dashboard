package com.example.demo.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for AI insights
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {
    private String topic;
    private String summary;
    private List<String> keyConcepts;
    private List<Recommendation> recommendations;
    private Instant timestamp;
}

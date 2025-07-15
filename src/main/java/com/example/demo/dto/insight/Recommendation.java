package com.example.demo.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a recommendation from the AI analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private String title;
    private String description;
}

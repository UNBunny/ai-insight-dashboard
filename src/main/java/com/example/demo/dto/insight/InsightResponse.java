package com.example.demo.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {
    private String summary;
    private List<String> keyConcepts;
    private List<ResourceLink> furtherReading;
}

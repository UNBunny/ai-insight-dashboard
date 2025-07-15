package com.example.demo.dto.insight;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на AI-анализ темы или текста
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightRequest {
    
    @NotBlank(message = "Тема для анализа обязательна")
    @Size(min = 2, max = 200, message = "Тема должна содержать от 2 до 200 символов")
    private String topic;
    
    /**
     * Текст для анализа. Если указан, то анализ проводится с его учетом,
     * иначе анализируется только тема.
     */
    @Size(max = 5000, message = "Текст не должен превышать 5000 символов")
    private String text;
    
    @Min(value = 1, message = "Минимальное количество результатов: 1")
    @Max(value = 50, message = "Максимальное количество результатов: 50")
    private Integer maxResults;
    
    @Pattern(regexp = "^(ru|en|es|fr|de)?$", message = "Поддерживаемые языки: ru, en, es, fr, de или пустое значение")
    private String language;
}

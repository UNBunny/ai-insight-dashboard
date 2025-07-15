-- Schema for AI analysis cache

CREATE TABLE ai_analysis_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic VARCHAR(255) NOT NULL,
    language VARCHAR(50) NOT NULL DEFAULT 'en',
    analysis_content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    access_count INT DEFAULT 1
);

-- Composite index for lookup by topic and language
CREATE INDEX idx_topic_language ON ai_analysis_cache (topic, language);

-- Index for cleanup of old cache entries
CREATE INDEX idx_last_accessed ON ai_analysis_cache (last_accessed_at);

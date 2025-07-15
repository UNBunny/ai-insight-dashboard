package com.example.demo.controllers;

import com.example.demo.services.client.OllamaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для проверки статуса и управления Ollama
 */
@RestController
@RequestMapping("/system/ollama")
@RequiredArgsConstructor
@Slf4j
public class OllamaStatusController {

    private final OllamaClient ollamaClient;
    
    /**
     * Получение статуса Ollama и доступных моделей
     *
     * @return Информация о состоянии Ollama
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getOllamaStatus() {
        log.info("Запрошен статус Ollama");
        
        Map<String, Object> status = new HashMap<>();
        boolean isAvailable = ollamaClient.isAvailable();
        
        status.put("available", isAvailable);
        status.put("provider", ollamaClient.getProviderName());
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Перепроверка доступности Ollama
     * 
     * @return Обновленная информация о состоянии Ollama
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> recheckOllamaStatus() {
        log.info("Запрошена перепроверка Ollama");
        
        boolean checkResult = ollamaClient.checkOllamaAvailability();
        
        Map<String, Object> status = new HashMap<>();
        status.put("available", checkResult);
        status.put("provider", ollamaClient.getProviderName());
        
        return ResponseEntity.ok(status);
    }
}

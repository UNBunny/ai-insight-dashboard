package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа аутентификации
 * Включает в себя JWT токен при успешной аутентификации или сообщение об ошибке
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Поля с null значениями не будут включены в JSON
public class AuthResponse {
    private String token;
    private String error;
    private boolean success;
    private List<String> roles;
    private String username;
    
    /**
     * Конструктор для успешного ответа с токеном
     * 
     * @param token JWT токен доступа
     */
    public AuthResponse(String token) {
        this.token = token;
        this.success = true;
        this.error = null;
    }
    
    /**
     * Конструктор для успешного ответа с токеном и ролями
     * 
     * @param token JWT токен доступа
     * @param username Имя пользователя
     * @param roles Список ролей пользователя
     */
    public AuthResponse(String token, String username, List<String> roles) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.success = true;
        this.error = null;
    }
    
    /**
     * Конструктор для ответа с ошибкой
     * 
     * @param token JWT токен (может быть null)
     * @param error Сообщение об ошибке
     */
    public AuthResponse(String token, String error) {
        this.token = token;
        this.error = error;
        this.success = (token != null);
    }
}

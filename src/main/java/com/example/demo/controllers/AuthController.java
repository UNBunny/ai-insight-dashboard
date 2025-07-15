package com.example.demo.controllers;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.security.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

/**
 * Контроллер для аутентификации пользователей
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "${spring.webmvc.cors.allowed-origins:*}")
@Tag(name = "Authentication", description = "API для аутентификации и получения JWT токенов")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Endpoint для аутентификации и получения JWT токена
     * 
     * @param authRequest данные для аутентификации
     * @return JWT токен при успешной аутентификации
     */
    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение JWT токена")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Попытка авторизации для пользователя: {}", authRequest.getUsername());
        log.debug("Context path: {}", System.getProperty("server.servlet.context-path"));
        
        try {
            // Создаем токен аутентификации
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), 
                authRequest.getPassword()
            );
            
            log.debug("Попытка аутентификации для пользователя: {}, с паролем длиной: {}", 
                     authRequest.getUsername(), 
                     authRequest.getPassword() != null ? authRequest.getPassword().length() : 0);
            
            // Пытаемся аутентифицировать пользователя
            Authentication authentication = authenticationManager.authenticate(authToken);
            log.debug("Аутентификация прошла успешно");
            
            // Если успешно, получаем пользовательские детали
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.debug("Получены детали пользователя: {}, роли: {}", 
                     userDetails.getUsername(), 
                     userDetails.getAuthorities());
            
            // Генерируем JWT токен
            final String token = jwtTokenUtil.generateToken(userDetails);
            log.info("Успешная аутентификация для пользователя: {}", userDetails.getUsername());
            
            // Извлекаем роли из пользовательских деталей
            List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            
            // Возвращаем токен в ответе с информацией о ролях
            AuthResponse authResponse = new AuthResponse(token, userDetails.getUsername(), roles);
            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            log.warn("Неверные учетные данные для пользователя: {}, причина: {}", 
                  authRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body(new AuthResponse(null, "Invalid username or password"));
        } catch (Exception e) {
            log.error("Ошибка при аутентификации: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new AuthResponse(null, "Authentication error: " + e.getMessage()));
        }
    }
}

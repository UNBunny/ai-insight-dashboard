package com.example.demo.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для обработки JWT токенов в каждом запросе
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
    
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final String BEARER_PREFIX = "Bearer ";

    // Список публичных эндпоинтов, которые не требуют JWT авторизации
    // Пути указываются без префикса /api/v1, который добавляется автоматически
    private static final String[] PUBLIC_ENDPOINTS = {
        "/h2-console", 
        "/actuator/health",
        "/actuator/info",
        "/auth/login",
        "/auth/register",
        "/api-docs", 
        "/v1/api-docs",
        "/v3/api-docs",
        "/swagger-ui",
        "/swagger-ui.html",
        "/swagger-resources",
        "/ai/analyze",  // Публичный AI эндпоинт
        "/ai/test"      // Тестовый AI эндпоинт
    };
    
    /**
     * Определяет, является ли запрошенный эндпоинт публичным (не требующим авторизации)
     * 
     * @param request HTTP запрос
     * @return true, если эндпоинт публичный
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String contextPath = "/api/v1";
        
        // Если путь содержит контекстный путь, удаляем его для сравнения
        String normalizedPath = requestPath;
        if (requestPath.startsWith(contextPath)) {
            normalizedPath = requestPath.substring(contextPath.length());
        }
        
        logger.debug("Checking if path is public: {} (normalized: {})", requestPath, normalizedPath);
        
        // Проверяем на публичные эндпоинты
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (normalizedPath.equals(publicEndpoint) || 
                normalizedPath.startsWith(publicEndpoint + "/")) {
                logger.debug("Public endpoint match found: {} for path: {}", publicEndpoint, requestPath);
                return true;
            }
        }
        
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Логируем запрос для отладки
        logger.debug("Processing request to path: {}", request.getRequestURI());
        
        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT токен находится в заголовке Authorization с префиксом "Bearer "
        if (requestTokenHeader != null && requestTokenHeader.startsWith(BEARER_PREFIX)) {
            jwtToken = requestTokenHeader.substring(BEARER_PREFIX.length());
            logger.debug("JWT token extracted: {}", jwtToken.substring(0, Math.min(10, jwtToken.length())) + "...");
            
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                logger.debug("Successfully extracted username: {}", username);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get JWT Token: {}", e.getMessage());
            } catch (ExpiredJwtException e) {
                logger.error("JWT Token has expired: {}", e.getMessage());
                // Отправляем специальный заголовок для фронтенда
                response.setHeader("X-JWT-Expired", "true");
            } catch (Exception e) {
                logger.error("JWT Token processing error: {}", e.getMessage(), e);
            }
        } else {
            // Не логируем для публичных эндпоинтов, чтобы не засорять журнал
            if (!isPublicEndpoint(request)) {
                logger.debug("JWT Token does not begin with Bearer String or is missing for protected path: {}", request.getRequestURI());
            }
        }
        
        // Логируем все заголовки для отладки
        if (logger.isTraceEnabled()) {
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                logger.trace("Header: {} = {}", headerName, request.getHeader(headerName));
            }
        }

        // Валидация токена
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Если токен валиден, настраиваем аутентификацию Spring Security
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Устанавливаем аутентификацию в контексте
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Authentication set for user {}", username);
            }
        }
        chain.doFilter(request, response);
    }
}

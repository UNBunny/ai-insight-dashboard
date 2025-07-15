package com.example.demo.config;

import com.example.demo.security.JwtAuthEntryPoint;
import com.example.demo.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация безопасности веб-приложения
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    
    @Autowired
    private JwtAuthEntryPoint jwtAuthEntryPoint;
    
    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF для REST API
                .csrf(csrf -> csrf.disable())
                
                // Настраиваем CORS
                .cors(cors -> {})
                
                // Обработка исключений аутентификации
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))
                
                // Настраиваем управление сессиями (STATELESS для REST с JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Настраиваем авторизацию запросов
                .authorizeHttpRequests(auth -> auth
                    // Публичные эндпоинты без аутентификации
                    .requestMatchers(
                            "/*/h2-console/**",
                            "/*/actuator/health",
                            "/*/actuator/info",
                            "/*/auth/login",
                            "/*/auth/register",
                            "/*/api-docs/**",
                            "/*/swagger-ui/**",
                            "/*/swagger-ui.html",
                            "/*/ai/analyze/**",     // Public AI analysis endpoint
                            "/*/ai/test/**",        // Test endpoints
                            // Дублируем пути для поддержки как с контекстом, так и без него
                            "/h2-console/**",
                            "/actuator/health",
                            "/actuator/info",
                            "/auth/login",
                            "/auth/register",
                            "/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/ai/analyze/**", 
                            "/ai/test/**"
                    ).permitAll()
                    
                    // Эндпоинты доступные только для администраторов
                    .requestMatchers(
                            "/*/users/**",     // Admin panel user management
                            "/*/admin/**",    // Any admin-specific endpoints
                            "/*/ai/manage/**", // AI content management endpoints
                            "/*/actuator/**",        // Actuator endpoints
                            "/*/admin/**",            // Admin panel URLs
                            // Дублируем пути для поддержки как с контекстом, так и без него
                            "/users/**",
                            "/admin/**",
                            "/ai/manage/**",
                            "/actuator/**",
                            "/admin/**"
                    ).hasRole("ADMIN")
                    
                    // Все остальные запросы требуют аутентификации
                    .anyRequest().authenticated())
                
                // Для H2 консоли
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        
        // Добавляем JWT фильтр перед фильтром UsernamePasswordAuthenticationFilter
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

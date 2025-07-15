package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Удален неиспользуемый импорт
import java.util.List;

/**
 * Конфигурация для OpenAPI/Swagger документации API
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    /**
     * Константа с версией OpenAPI
     */
    private static final String OPENAPI_VERSION = "3.0.1";

    /**
     * Конфигурирует и создает объект OpenAPI для документации API
     * 
     * @return сконфигурированный объект OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        // Явно указываем версию OpenAPI
        OpenAPI openAPI = new OpenAPI();
        openAPI.addExtension("x-openapi-version", OPENAPI_VERSION);
        
        // Добавляем основную информацию
        Info info = new Info()
                .title("AI Insight Dashboard API")
                .version("1.0.0")
                .description("REST API для AI Insight Dashboard, позволяющая анализировать темы с использованием AI")
                .contact(new Contact()
                        .name("AI Insight Team")
                        .email("support@ai-insight-dashboard.example.com")
                        .url("https://ai-insight-dashboard.example.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
        
        // Устанавливаем информацию в объект OpenAPI
        openAPI.setInfo(info);
        
        // Добавляем информацию о серверах
        String serverUrl = "http://localhost:" + serverPort;
        if (contextPath != null && !contextPath.isEmpty()) {
            serverUrl += contextPath;
        }
        
        Server localServer = new Server()
                .url(serverUrl)
                .description("Локальный сервер разработки");
        
        Server productionServer = new Server()
                .url("https://api.ai-insight-dashboard.example.com")
                .description("Продакшн сервер");
                
        openAPI.setServers(List.of(localServer, productionServer));
        
        // Настраиваем схемы безопасности
        Components components = new Components();
        
        SecurityScheme bearerAuth = new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT аутентификация с использованием Bearer токена");
        
        components.addSecuritySchemes("bearerAuth", bearerAuth);
        openAPI.setComponents(components);
        
        // Добавляем требования безопасности глобально
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList("bearerAuth");
        openAPI.addSecurityItem(securityRequirement);
        
        return openAPI;
    }
}

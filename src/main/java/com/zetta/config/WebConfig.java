package com.zetta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for CORS settings.
 * Allows configurable origins to access the API via the CORS_ALLOWED_ORIGINS environment variable.
 */
@Configuration
public class WebConfig {

    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins(corsAllowedOrigins.split(","))
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
                    .allowCredentials(true);
            }
        };
    }
}


package com.zetta.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Configuration for Jackson JSON serialization/deserialization.
 * Ensures the API accepts and sends snake_case JSON while using camelCase in Java.
 * 
 * Example: JSON {"user_id": "123"} maps to Java field userId
 */
@Configuration
public class JacksonConfig {
    
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();
    }
}


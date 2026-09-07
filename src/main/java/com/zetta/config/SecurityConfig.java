package com.zetta.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Security configuration for the SplitBro API.
 * Configures CORS, CSRF, and Supabase JWT validation.
 * 
 * Uses SupabaseAuthFilter to validate tokens by calling Supabase's /auth/v1/user endpoint,
 * since Supabase uses ES256 and doesn't expose a JWKS endpoint.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private SupabaseAuthFilter supabaseAuthFilter;

    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;

    /**
     * Request logging filter for debugging - logs all incoming requests.
     * Only logs at DEBUG level to avoid noise in production.
     */
    @Bean
    public OncePerRequestFilter requestLoggingFilter() {
        final Logger log = logger; // capture outer class logger
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                    FilterChain filterChain) throws ServletException, IOException {
                if (log.isDebugEnabled()) {
                    String authHeader = request.getHeader("Authorization");
                    log.debug("REQUEST: {} {} | Origin: {} | Auth: {}",
                        request.getMethod(), request.getRequestURI(),
                        request.getHeader("Origin"),
                        authHeader != null ? "present (" + authHeader.length() + " chars)" : "MISSING");
                }
                
                filterChain.doFilter(request, response);
            }
        };
    }

    /**
     * Standalone CorsFilter bean - handles CORS at highest priority before security filters.
     * This ensures pre-flight OPTIONS requests and CORS headers are processed first.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Allowed origins from environment configuration
        List<String> origins = Arrays.asList(corsAllowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        
        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);
        
        logger.info("CORS initialized with allowed origins: {}", origins);
        
        // Allowed HTTP methods - all methods
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allowed headers
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Expose headers to the client
        config.setExposedHeaders(Arrays.asList(
            "Authorization"
        ));
        
        // Cache pre-flight response for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Add request logging filter first
            .addFilterBefore(requestLoggingFilter(), UsernamePasswordAuthenticationFilter.class)
            // Add Supabase auth filter to validate tokens
            .addFilterBefore(supabaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // Enable CORS
            .cors(cors -> cors.configure(http))
            // Disable CSRF for stateless REST API using JWTs
            .csrf(csrf -> csrf.disable())
            // Stateless session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Configure endpoint authorization
            .authorizeHttpRequests(auth -> auth
                // Permit all OPTIONS requests (pre-flight) without authentication
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Health check endpoints - public
                .requestMatchers("/", "/health", "/actuator/health").permitAll()
                // All API requests require authentication
                .requestMatchers("/api/**").authenticated()
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }
}



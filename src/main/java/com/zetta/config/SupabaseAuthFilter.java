package com.zetta.config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom filter to validate Supabase JWT tokens by calling the Supabase Auth API.
 * 
 * Since Supabase uses ES256 and doesn't expose a JWKS endpoint, we validate
 * tokens by calling GET /auth/v1/user with the Bearer token.
 * 
 * If the token is valid, Supabase returns the user info.
 * If invalid, Supabase returns 401.
 */
@Component
public class SupabaseAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthFilter.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        // Skip if no auth header or not a Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authHeader.substring(7);
        
        try {
            // Call Supabase to validate the token
            HttpRequest supabaseRequest = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/auth/v1/user"))
                .header("Authorization", "Bearer " + accessToken)
                .header("apikey", supabaseAnonKey)
                .GET()
                .build();

            HttpResponse<String> supabaseResponse = httpClient.send(supabaseRequest, 
                HttpResponse.BodyHandlers.ofString());

            if (supabaseResponse.statusCode() == 200) {
                // Token is valid - parse user info
                JsonNode userJson = objectMapper.readTree(supabaseResponse.body());
                String userId = userJson.get("id").asText();
                String email = userJson.has("email") ? userJson.get("email").asText() : "";
                
                logger.debug("Valid token for user: {} ({})", userId, email);
                
                // Create authentication with user ID as principal
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userId, 
                        null, 
                        AuthorityUtils.createAuthorityList("ROLE_USER")
                    );
                
                // Store user details for controllers to access
                authentication.setDetails(userJson.toString());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.warn("Invalid token - Supabase returned: {}", supabaseResponse.statusCode());
                // Don't set authentication - let Spring Security handle 401
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Request interrupted: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't filter OPTIONS requests (CORS preflight)
        return "OPTIONS".equals(request.getMethod());
    }
}

package com.ritapinto.radiogaga.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private final byte[] apiKey;

    public ApiKeyFilter(@Value("${radiogaga.api-key}") String apiKey) {
        this.apiKey = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean isStations = request.getRequestURI().startsWith("/stations");
        boolean isRead = "GET".equals(request.getMethod());
        return !isStations || isRead;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String provided = request.getHeader("X-API-Key");

        if (provided == null
                || !MessageDigest.isEqual(provided.getBytes(StandardCharsets.UTF_8), apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/problem+json");
            response.getWriter().write(
                    "{\"status\":401,\"title\":\"Unauthorized\","
                    + "\"detail\":\"API key em falta ou inválida\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}

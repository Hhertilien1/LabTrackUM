package com.um.labtrack.config;

import com.um.labtrack.service.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to handle authentication via X-Username header.
 * Sets the session in AuthService based on the username header.
 */
@Component
@Order(1)
public class AuthFilter implements Filter {

    private final AuthService authService;

    @Autowired
    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String username = httpRequest.getHeader("X-Username");
        
        // Set session if username header is present
        if (username != null && !username.trim().isEmpty()) {
            authService.setSessionByUsername(username);
        }
        
        chain.doFilter(request, response);
    }
}

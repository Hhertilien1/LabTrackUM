package com.um.labtrack.controller;

import com.um.labtrack.dto.LoginRequest;
import com.um.labtrack.dto.UserDTO;
import com.um.labtrack.entity.User;
import com.um.labtrack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication operations.
 * Provides endpoints for login, logout, and session management.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login endpoint.
     *
     * @param request Login request DTO containing username and password
     * @return ResponseEntity with session information
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        boolean success = authService.login(request.getUsername(), request.getPassword());
        Map<String, Object> response = new HashMap<>();

        if (success) {
            User user = authService.getCurrentUser();
            
            // Convert to DTO (exclude password)
            UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getIsTA(),
                user.getCreatedAt() != null ? user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            );
            
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", userDTO);
            response.put("role", user.getRole().toString());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Logout endpoint.
     *
     * @return ResponseEntity with logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        authService.logout();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    /**
     * Get current session information.
     *
     * @return ResponseEntity with current session data
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSession() {
        Map<String, Object> response = new HashMap<>();
        if (authService.isAuthenticated()) {
            User user = authService.getCurrentUser();
            
            // Convert to DTO (exclude password)
            UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getIsTA(),
                user.getCreatedAt() != null ? user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            );
            
            response.put("authenticated", true);
            response.put("user", userDTO);
            response.put("role", user.getRole().toString());
            response.put("canManageLocationsAndEquipment", authService.canManageLocationsAndEquipment());
        } else {
            response.put("authenticated", false);
        }
        return ResponseEntity.ok(response);
    }
}

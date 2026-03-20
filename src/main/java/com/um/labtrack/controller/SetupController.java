package com.um.labtrack.controller;

import com.um.labtrack.dto.SetupAdminRequest;
import com.um.labtrack.dto.UserDTO;
import com.um.labtrack.entity.User;
import com.um.labtrack.service.SetupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for initial setup operations.
 * Provides endpoints for first-run admin bootstrap.
 * These endpoints are only accessible when no users exist in the system.
 */
@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private final SetupService setupService;

    @Autowired
    public SetupController(SetupService setupService) {
        this.setupService = setupService;
    }

    /**
     * Check if initial setup is required.
     *
     * @return ResponseEntity with setup required status
     */
    @GetMapping("/required")
    public ResponseEntity<Map<String, Boolean>> isSetupRequired() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("required", setupService.isSetupRequired());
        return ResponseEntity.ok(response);
    }

    /**
     * Migrate database schema (adds missing columns).
     * This is a one-time migration endpoint for testing.
     *
     * @return ResponseEntity with migration status
     */
    @PostMapping("/debug/migrate")
    public ResponseEntity<?> migrateDatabase() {
        try {
            setupService.migrateDatabase();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Database migration completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("message", "Migration failed: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all users with passwords (for testing/debugging only).
     * This endpoint is only accessible when setup is required or for debugging.
     *
     * @return ResponseEntity with list of all users including password hashes
     */
    @GetMapping("/debug/users")
    public ResponseEntity<?> getAllUsersForDebug() {
        try {
            List<Map<String, Object>> users = setupService.getAllUsersForDebug();
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("count", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("message", "Failed to retrieve users. Error: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            error.put("hint", "Try calling POST /api/setup/debug/migrate first to add missing columns");
            e.printStackTrace(); // Log for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Create initial admin user.
     * This endpoint is only accessible when setup is required.
     *
     * @param request Setup admin request with user details
     * @return ResponseEntity with created admin user or error
     */
    @PostMapping("/admin")
    public ResponseEntity<?> createInitialAdmin(@Valid @RequestBody SetupAdminRequest request) {
        try {
            // Validate password confirmation
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Passwords do not match");
                return ResponseEntity.badRequest().body(error);
            }

            User admin = setupService.createInitialAdmin(
                request.getUsername(),
                request.getPassword(),
                request.getFullName(),
                request.getEmail()
            );

            // Convert to DTO (exclude password)
            UserDTO userDTO = new UserDTO(
                admin.getId(),
                admin.getUsername(),
                admin.getFullName(),
                admin.getEmail(),
                admin.getRole(),
                admin.getActive(),
                admin.getIsTA(),
                admin.getCreatedAt() != null ? admin.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Admin user created successfully");
            response.put("user", userDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

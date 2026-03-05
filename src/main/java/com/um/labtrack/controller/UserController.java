package com.um.labtrack.controller;

import com.um.labtrack.dto.CreateUserRequest;
import com.um.labtrack.dto.UserDTO;
import com.um.labtrack.entity.User;
import com.um.labtrack.service.AuthService;
import com.um.labtrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for User management operations.
 * Provides endpoints for CRUD operations on users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * Constructor-based dependency injection.
     *
     * @param userService Service for user business logic
     * @param authService Service for authentication
     */
    @Autowired
    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Check if user is authenticated.
     *
     * @return ResponseEntity with error if not authenticated
     */
    private ResponseEntity<?> checkAuthentication() {
        if (!authService.isAuthenticated()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        return null;
    }

    /**
     * Get all users.
     * Returns users as DTOs (passwords excluded).
     *
     * @return ResponseEntity containing list of all users as DTOs
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        ResponseEntity<?> authCheck = checkAuthentication();
        if (authCheck != null) return authCheck;
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
            .map(user -> new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getIsTA(),
                user.getCreatedAt() != null ? user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Get user by ID.
     *
     * @param id User ID
     * @return ResponseEntity containing the user if found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new user.
     *
     * @param request CreateUserRequest DTO
     * @return ResponseEntity containing the created user as DTO
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        ResponseEntity<?> authCheck = checkAuthentication();
        if (authCheck != null) return authCheck;
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword()); // Will be hashed in service
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setRole(request.getRole());
            user.setIsTA(request.getIsTA()); // Set TA flag if provided
            
            User createdUser = userService.createUser(user);
            
            // Convert to DTO
            UserDTO userDTO = new UserDTO(
                createdUser.getId(),
                createdUser.getUsername(),
                createdUser.getFullName(),
                createdUser.getEmail(),
                createdUser.getRole(),
                createdUser.getActive(),
                createdUser.getIsTA(),
                createdUser.getCreatedAt() != null ? createdUser.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update an existing user.
     *
     * @param id   User ID to update
     * @param user Updated user data
     * @return ResponseEntity containing the updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a user by ID.
     * ADMIN users cannot be deleted.
     *
     * @param id User ID to delete
     * @return ResponseEntity with no content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        ResponseEntity<?> authCheck = checkAuthentication();
        if (authCheck != null) return authCheck;
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Deactivate a user (soft delete).
     * ADMIN users cannot be deactivated.
     *
     * @param id User ID to deactivate
     * @return ResponseEntity with deactivated user as DTO
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        ResponseEntity<?> authCheck = checkAuthentication();
        if (authCheck != null) return authCheck;
        try {
            User deactivatedUser = userService.deactivateUser(id);
            UserDTO userDTO = new UserDTO(
                deactivatedUser.getId(),
                deactivatedUser.getUsername(),
                deactivatedUser.getFullName(),
                deactivatedUser.getEmail(),
                deactivatedUser.getRole(),
                deactivatedUser.getActive(),
                deactivatedUser.getIsTA(),
                deactivatedUser.getCreatedAt() != null ? deactivatedUser.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            );
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Promote a STUDENT to TA.
     * Only TEACHER can perform this action.
     *
     * @param id Student ID to promote
     * @return ResponseEntity with promoted user as DTO
     */
    @PostMapping("/{id}/promote-to-ta")
    public ResponseEntity<?> promoteStudentToTA(@PathVariable Long id) {
        ResponseEntity<?> authCheck = checkAuthentication();
        if (authCheck != null) return authCheck;
        try {
            User promotedUser = userService.promoteStudentToTA(id);
            UserDTO userDTO = new UserDTO(
                promotedUser.getId(),
                promotedUser.getUsername(),
                promotedUser.getFullName(),
                promotedUser.getEmail(),
                promotedUser.getRole(),
                promotedUser.getActive(),
                promotedUser.getIsTA(),
                promotedUser.getCreatedAt() != null ? promotedUser.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            );
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Demote a STUDENT from TA (sets isTA flag to false).
     * Only TEACHER or ADMIN can perform this action.
     *
     * @param id Student ID to demote
     * @return ResponseEntity with updated user as DTO
     */
    @PostMapping("/{id}/demote-from-ta")
    public ResponseEntity<?> demoteStudentFromTA(@PathVariable Long id) {
        ResponseEntity<?> authCheck = checkAuthentication();
        if (authCheck != null) return authCheck;
        try {
            User user = userService.setStudentTAFlag(id, false);
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
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

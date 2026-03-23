package com.um.labtrack.controller;

import com.um.labtrack.entity.Equipment;
import com.um.labtrack.service.AuthService;
import com.um.labtrack.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Equipment management operations.
 * Provides endpoints for CRUD operations and check-in/check-out functionality.
 */
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final AuthService authService;

    /**
     * Constructor-based dependency injection.
     *
     * @param equipmentService Service for equipment business logic
     * @param authService      Service for authentication
     */
    @Autowired
    public EquipmentController(EquipmentService equipmentService, AuthService authService) {
        this.equipmentService = equipmentService;
        this.authService = authService;
    }

    private ResponseEntity<?> checkAuth() {
        if (!authService.isAuthenticated()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        return null;
    }

    /**
     * Get all equipment.
     *
     * @return ResponseEntity containing list of all equipment
     */
    @GetMapping
    public ResponseEntity<?> getAllEquipment() {
        ResponseEntity<?> authCheck = checkAuth();
        if (authCheck != null) return authCheck;
        try {
            List<Equipment> equipment = equipmentService.getAllEquipment();
            return ResponseEntity.ok(equipment);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve equipment");
            error.put("message", e.getMessage());
            error.put("hint", "Tables may not exist. Try calling POST /api/setup/debug/migrate to create missing tables.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get equipment by ID.
     *
     * @param id Equipment ID
     * @return ResponseEntity containing the equipment if found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        return equipmentService.getEquipmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new equipment item.
     *
     * @param equipment Equipment entity to create
     * @return ResponseEntity containing the created equipment
     */
    @PostMapping
    public ResponseEntity<?> createEquipment(@RequestBody Equipment equipment) {
        ResponseEntity<?> authCheck = checkAuth();
        if (authCheck != null) return authCheck;
        try {
            Equipment createdEquipment = equipmentService.createEquipment(equipment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEquipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing equipment item.
     *
     * @param id        Equipment ID to update
     * @param equipment Updated equipment data
     * @return ResponseEntity containing the updated equipment
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEquipment(@PathVariable Long id, @RequestBody Equipment equipment) {
        ResponseEntity<?> authCheck = checkAuth();
        if (authCheck != null) return authCheck;
        try {
            Equipment updatedEquipment = equipmentService.updateEquipment(id, equipment);
            return ResponseEntity.ok(updatedEquipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete equipment by ID.
     *
     * @param id Equipment ID to delete
     * @return ResponseEntity with no content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEquipment(@PathVariable Long id) {
        ResponseEntity<?> authCheck = checkAuth();
        if (authCheck != null) return authCheck;
        try {
            equipmentService.deleteEquipment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check out equipment to a user.
     *
     * @param id      Equipment ID to check out
     * @param request Request body containing userId
     * @return ResponseEntity containing the updated equipment
     */
    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> checkoutEquipment(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        ResponseEntity<?> authCheck = checkAuth();
        if (authCheck != null) return authCheck;
        try {
            Long userId = request.get("userId");
            if (userId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Select a student to assign the equipment to");
                return ResponseEntity.badRequest().body(error);
            }
            Equipment equipment = equipmentService.checkoutEquipment(id, userId);
            return ResponseEntity.ok(equipment);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Check in equipment from the student who had it.
     * userId is optional: if provided, must be the current holder; if omitted, the current holder is used.
     *
     * @param id      Equipment ID to check in
     * @param request Request body optionally containing userId (student from whom equipment is collected)
     * @return ResponseEntity containing the updated equipment
     */
    @PostMapping("/{id}/checkin")
    public ResponseEntity<?> checkinEquipment(@PathVariable Long id, @RequestBody(required = false) Map<String, Long> request) {
        ResponseEntity<?> authCheck = checkAuth();
        if (authCheck != null) return authCheck;
        try {
            Long userId = request != null ? request.get("userId") : null;
            Equipment equipment = equipmentService.checkinEquipment(id, userId);
            return ResponseEntity.ok(equipment);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

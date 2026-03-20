package com.um.labtrack.controller;

import com.um.labtrack.entity.Location;
import com.um.labtrack.service.AuthService;
import com.um.labtrack.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Location management operations.
 * Provides endpoints for creating and retrieving locations.
 */
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;
    private final AuthService authService;

    /**
     * Constructor-based dependency injection.
     *
     * @param locationService Service for location business logic
     * @param authService     Service for authentication
     */
    @Autowired
    public LocationController(LocationService locationService, AuthService authService) {
        this.locationService = locationService;
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
     * Get all locations.
     *
     * @return ResponseEntity containing list of all locations
     */
    @GetMapping
    public ResponseEntity<?> getAllLocations() {
        ResponseEntity<?> authCheck = checkAuth();
        if (authCheck != null) return authCheck;
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    /**
     * Create a new location.
     *
     * @param location Location entity to create
     * @return ResponseEntity containing the created location
     */
    @PostMapping
    public ResponseEntity<?> createLocation(@RequestBody Location location) {
        ResponseEntity<?> authCheck = checkAuth();
        if (authCheck != null) return authCheck;
        Location createdLocation = locationService.createLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLocation);
    }
}

package com.um.labtrack.service;

import com.um.labtrack.entity.Location;
import com.um.labtrack.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Location business logic.
 * Handles location management and queries.
 */
@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;

    /**
     * Constructor-based dependency injection.
     *
     * @param locationRepository Repository for location data access
     */
    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * Retrieve all locations from the database.
     *
     * @return List of all locations
     */
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    /**
     * Find location by ID.
     *
     * @param id The location ID
     * @return Optional containing the Location if found
     */
    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }

    /**
     * Create a new location.
     *
     * @param location The location entity to save
     * @return The saved location entity with generated ID
     */
    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }

    /**
     * Find or create a location by building, room, and cabinet.
     *
     * @param building Building name
     * @param room     Room number
     * @param cabinet  Cabinet identifier (can be null)
     * @return The existing or newly created location
     */
    public Location findOrCreateLocation(String building, String room, String cabinet) {
        if (cabinet != null && !cabinet.isEmpty()) {
            Optional<Location> existing = locationRepository.findByBuildingAndRoomAndCabinet(building, room, cabinet);
            if (existing.isPresent()) {
                return existing.get();
            }
            return locationRepository.save(new Location(building, room, cabinet));
        } else {
            List<Location> existing = locationRepository.findByBuildingAndRoom(building, room);
            if (!existing.isEmpty()) {
                return existing.get(0);
            }
            return locationRepository.save(new Location(building, room));
        }
    }

    /**
     * Find all locations by building.
     *
     * @param building Building name
     * @return List of locations in the building
     */
    public List<Location> getLocationsByBuilding(String building) {
        return locationRepository.findByBuilding(building);
    }
}

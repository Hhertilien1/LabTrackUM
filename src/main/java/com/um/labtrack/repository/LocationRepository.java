package com.um.labtrack.repository;

import com.um.labtrack.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Location entity operations.
 * Provides CRUD operations and custom query methods for location management.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Find location by building, room, and cabinet.
     *
     * @param building Building name
     * @param room     Room number
     * @param cabinet  Cabinet identifier
     * @return Optional containing the Location if found
     */
    Optional<Location> findByBuildingAndRoomAndCabinet(String building, String room, String cabinet);

    /**
     * Find all locations by building.
     *
     * @param building Building name
     * @return List of locations in the specified building
     */
    List<Location> findByBuilding(String building);

    /**
     * Find all locations by building and room.
     *
     * @param building Building name
     * @param room     Room number
     * @return List of locations matching building and room
     */
    List<Location> findByBuildingAndRoom(String building, String room);
}

package com.um.labtrack.repository;

import com.um.labtrack.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository interface for Equipment entity operations.
 * Provides CRUD operations and custom query methods for equipment management.
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    /**
     * Find equipment by item number.
     *
     * @param itemNumber The unique item number
     * @return Optional containing the Equipment if found
     */
    Optional<Equipment> findByItemNumber(String itemNumber);

    /**
     * Check if equipment exists with the given item number.
     *
     * @param itemNumber The item number to check
     * @return true if equipment exists, false otherwise
     */
    boolean existsByItemNumber(String itemNumber);

    /**
     * Find all equipment by status.
     *
     * @param status The equipment status
     * @return List of equipment with the specified status
     */
    List<Equipment> findByStatus(Equipment.EquipmentStatus status);

    /**
     * Find all equipment by condition.
     *
     * @param condition The equipment condition
     * @return List of equipment with the specified condition
     */
    List<Equipment> findByCondition(Equipment.EquipmentCondition condition);
}

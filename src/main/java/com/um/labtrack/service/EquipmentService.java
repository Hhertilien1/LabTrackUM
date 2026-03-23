package com.um.labtrack.service;

import com.um.labtrack.entity.Equipment;
import com.um.labtrack.entity.Transaction;
import com.um.labtrack.entity.User;
import com.um.labtrack.repository.EquipmentRepository;
import com.um.labtrack.repository.LocationRepository;
import com.um.labtrack.repository.TransactionRepository;
import com.um.labtrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Equipment business logic.
 * Handles equipment management, check-in/check-out operations, and business rules.
 */
@Service
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuthService authService;

    /**
     * Constructor-based dependency injection.
     *
     * @param equipmentRepository   Repository for equipment data access
     * @param locationRepository   Repository for location data access
     * @param userRepository       Repository for user data access
     * @param transactionRepository Repository for transaction data access
     * @param authService          Authentication service for role checks
     */
    @Autowired
    public EquipmentService(EquipmentRepository equipmentRepository,
                            LocationRepository locationRepository,
                            UserRepository userRepository,
                            TransactionRepository transactionRepository,
                            AuthService authService) {
        this.equipmentRepository = equipmentRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.authService = authService;
    }

    /**
     * Only ADMIN, TEACHER, or STUDENT with TA flag can perform check-out/check-in.
     */
    private void requireCheckoutCheckinPermission() {
        if (!authService.isAuthenticated()) {
            throw new IllegalStateException("Authentication required to check out or check in equipment");
        }
        if (!authService.isAdmin() && !authService.isTeacher() && !authService.isTA()) {
            throw new IllegalArgumentException("Only Admin, Teacher, or TA can check out or check in equipment");
        }
    }

    /**
     * Retrieve all equipment from the database.
     *
     * @return List of all equipment
     */
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    /**
     * Find equipment by ID.
     *
     * @param id The equipment ID
     * @return Optional containing the Equipment if found
     */
    public Optional<Equipment> getEquipmentById(Long id) {
        return equipmentRepository.findById(id);
    }

    /**
     * Find equipment by item number.
     *
     * @param itemNumber The item number
     * @return Optional containing the Equipment if found
     */
    public Optional<Equipment> getEquipmentByItemNumber(String itemNumber) {
        return equipmentRepository.findByItemNumber(itemNumber);
    }

    /**
     * Create a new equipment item.
     *
     * @param equipment The equipment entity to save
     * @return The saved equipment entity with generated ID
     * @throws IllegalArgumentException if item number already exists
     */
    public Equipment createEquipment(Equipment equipment) {
        if (equipmentRepository.existsByItemNumber(equipment.getItemNumber())) {
            throw new IllegalArgumentException("Item number already exists: " + equipment.getItemNumber());
        }
        return equipmentRepository.save(equipment);
    }

    /**
     * Update an existing equipment item.
     *
     * @param id        The equipment ID to update
     * @param equipment The updated equipment data
     * @return The updated equipment entity
     * @throws IllegalArgumentException if equipment not found
     */
    public Equipment updateEquipment(Long id, Equipment equipment) {
        Equipment existingEquipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with id: " + id));

        existingEquipment.setName(equipment.getName());
        existingEquipment.setCondition(equipment.getCondition());
        existingEquipment.setLocation(equipment.getLocation());

        return equipmentRepository.save(existingEquipment);
    }

    /**
     * Delete equipment by ID.
     *
     * @param id The equipment ID to delete
     * @throws IllegalArgumentException if equipment not found
     */
    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Equipment not found with id: " + id);
        }
        equipmentRepository.deleteById(id);
    }

    /**
     * Check out equipment to a student.
     * Only Admin, Teacher, or TA can perform. The assignee (userId) must be a STUDENT.
     *
     * @param equipmentId The equipment ID to check out
     * @param userId      The student user ID to assign the equipment to (who will have the equipment)
     * @return The updated equipment entity
     */
    public Equipment checkoutEquipment(Long equipmentId, Long userId) {
        requireCheckoutCheckinPermission();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with id: " + equipmentId));

        if (equipment.getStatus() == Equipment.EquipmentStatus.CHECKED_OUT) {
            throw new IllegalStateException("Equipment is already checked out");
        }

        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (assignee.getRole() != User.UserRole.STUDENT) {
            throw new IllegalArgumentException("Equipment can only be assigned to a student. Selected user is not a student.");
        }

        equipment.setStatus(Equipment.EquipmentStatus.CHECKED_OUT);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        Transaction transaction = new Transaction(savedEquipment, assignee, Transaction.TransactionAction.CHECKOUT);
        transactionRepository.save(transaction);

        return savedEquipment;
    }

    /**
     * Check in equipment from the student who had it (gather equipment from that student).
     * Only Admin, Teacher, or TA can perform. The userId is the student from whom equipment is being collected.
     * If the equipment is checked out to a different user, returns an error; otherwise accepts optional userId
     * and uses current holder when not provided.
     *
     * @param equipmentId The equipment ID to check in
     * @param userId      The student user ID from whom the equipment is being collected (must be current holder if provided)
     * @return The updated equipment entity
     */
    public Equipment checkinEquipment(Long equipmentId, Long userId) {
        requireCheckoutCheckinPermission();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with id: " + equipmentId));

        if (equipment.getStatus() == Equipment.EquipmentStatus.AVAILABLE) {
            throw new IllegalStateException("Equipment is already available");
        }

        Long currentHolderId = getCurrentHolderUserId(equipmentId);
        if (currentHolderId == null) {
            throw new IllegalStateException("Cannot determine who has this equipment; check transaction history.");
        }

        if (userId != null && !userId.equals(currentHolderId)) {
            throw new IllegalArgumentException("Equipment is checked out to a different student. Select the student who currently has the equipment.");
        }

        User holder = userRepository.findById(currentHolderId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + currentHolderId));

        equipment.setStatus(Equipment.EquipmentStatus.AVAILABLE);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        Transaction transaction = new Transaction(savedEquipment, holder, Transaction.TransactionAction.CHECKIN);
        transactionRepository.save(transaction);

        return savedEquipment;
    }

    /**
     * Returns the user ID of the student who currently has the equipment (from the latest CHECKOUT with no following CHECKIN).
     */
    public Long getCurrentHolderUserId(Long equipmentId) {
        List<Transaction> list = transactionRepository.findByEquipmentIdOrderByTimestampDesc(equipmentId);
        if (list.isEmpty()) return null;
        List<Transaction> chronological = new ArrayList<>(list);
        Collections.reverse(chronological);
        Long holder = null;
        for (Transaction t : chronological) {
            if (t.getAction() == Transaction.TransactionAction.CHECKOUT) {
                holder = t.getUser() != null ? t.getUser().getId() : null;
            } else if (t.getAction() == Transaction.TransactionAction.CHECKIN) {
                holder = null;
            }
        }
        return holder;
    }

    /**
     * Find all available equipment.
     *
     * @return List of available equipment
     */
    public List<Equipment> getAvailableEquipment() {
        return equipmentRepository.findByStatus(Equipment.EquipmentStatus.AVAILABLE);
    }

    /**
     * Find all checked out equipment.
     *
     * @return List of checked out equipment
     */
    public List<Equipment> getCheckedOutEquipment() {
        return equipmentRepository.findByStatus(Equipment.EquipmentStatus.CHECKED_OUT);
    }
}

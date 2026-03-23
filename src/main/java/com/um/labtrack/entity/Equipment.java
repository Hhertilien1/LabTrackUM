package com.um.labtrack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Equipment Entity representing lab equipment items in the inventory system.
 * Tracks item details including condition, status, and location.
 */
@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String itemNumber;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false)
    private EquipmentCondition condition;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = true)
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Enumeration for equipment condition states.
     * As per requirements: functional, broken, in repair.
     */
    public enum EquipmentCondition {
        FUNCTIONAL, BROKEN, IN_REPAIR
    }

    /**
     * Enumeration for equipment availability status.
     */
    public enum EquipmentStatus {
        AVAILABLE, CHECKED_OUT
    }

    /**
     * Default constructor required by JPA.
     */
    public Equipment() {
        this.status = EquipmentStatus.AVAILABLE;
    }

    /**
     * Constructor with required fields.
     *
     * @param itemNumber Unique item number identifier
     * @param name       Name/description of the equipment
     * @param condition  Condition of the equipment
     */
    public Equipment(String itemNumber, String name, EquipmentCondition condition) {
        this.itemNumber = itemNumber;
        this.name = name;
        this.condition = condition;
        this.status = EquipmentStatus.AVAILABLE;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EquipmentCondition getCondition() {
        return condition;
    }

    public void setCondition(EquipmentCondition condition) {
        this.condition = condition;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

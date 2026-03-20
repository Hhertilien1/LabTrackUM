package com.um.labtrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Location Entity representing physical locations where equipment is stored.
 * Tracks building, room number, and cabinet information.
 */
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String building;

    @Column(nullable = false)
    private String room;

    @Column(nullable = true)
    private String cabinet;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // Prevent circular reference when serializing Location
    private List<Equipment> equipmentList = new ArrayList<>();

    /**
     * Default constructor required by JPA.
     */
    public Location() {
    }

    /**
     * Constructor with required fields.
     *
     * @param building Building name or identifier
     * @param room     Room number or identifier
     * @param cabinet  Cabinet or shelf identifier (optional)
     */
    public Location(String building, String room, String cabinet) {
        this.building = building;
        this.room = room;
        this.cabinet = cabinet;
    }

    /**
     * Constructor without cabinet.
     *
     * @param building Building name or identifier
     * @param room     Room number or identifier
     */
    public Location(String building, String room) {
        this.building = building;
        this.room = room;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getCabinet() {
        return cabinet;
    }

    public void setCabinet(String cabinet) {
        this.cabinet = cabinet;
    }

    public List<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    /**
     * Returns a formatted string representation of the location.
     *
     * @return Formatted location string
     */
    public String getFormattedLocation() {
        if (cabinet != null && !cabinet.isEmpty()) {
            return building + " - " + room + " - " + cabinet;
        }
        return building + " - " + room;
    }
}

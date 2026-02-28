package com.um.labtrack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User Entity representing a Teaching Assistant or authorized user in the system.
 * This entity is used to track who checks items in and out of the inventory.
 * 
 * Future extensions: This can be expanded to include authentication, roles, and permissions.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "is_ta", nullable = false)
    private Boolean isTA = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Enumeration for user roles in the system.
     * Role hierarchy:
     * - ADMIN: Can create TEACHER and STUDENT, cannot be deleted
     * - TEACHER: Can create STUDENT, can set TA flag on STUDENT
     * - STUDENT: Regular student - Cannot create users
     * Note: TA is not a separate role, but a flag (isTA) on STUDENT users
     */
    public enum UserRole {
        ADMIN, TEACHER, STUDENT
    }

    /**
     * Default constructor required by JPA.
     */
    public User() {
    }

    /**
     * Constructor with required fields.
     *
     * @param username  Unique username for the user
     * @param fullName  Full name of the user
     * @param email     Email address of the user
     * @param password  Password for authentication
     * @param role      Role of the user (ADMIN, TEACHER, STUDENT)
     * @param isTA      Whether the user is a Teaching Assistant (only for STUDENT role)
     */
    public User(String username, String fullName, String email, String password, UserRole role, Boolean isTA) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isTA = isTA != null ? isTA : false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor with required fields (without isTA, defaults to false).
     *
     * @param username  Unique username for the user
     * @param fullName  Full name of the user
     * @param email     Email address of the user
     * @param password  Password for authentication
     * @param role      Role of the user (ADMIN, TEACHER, STUDENT)
     */
    public User(String username, String fullName, String email, String password, UserRole role) {
        this(username, fullName, email, password, role, false);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getIsTA() {
        return isTA != null ? isTA : false;
    }

    public void setIsTA(Boolean isTA) {
        this.isTA = isTA != null ? isTA : false;
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

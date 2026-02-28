package com.um.labtrack.dto;

import com.um.labtrack.entity.User;

/**
 * Data Transfer Object for User entity.
 * Used to transfer user data without exposing sensitive information or entity details.
 */
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private User.UserRole role;
    private Boolean active;
    private Boolean isTA;
    private String createdAt;

    public UserDTO() {
    }

    public UserDTO(Long id, String username, String fullName, String email, User.UserRole role, Boolean active, Boolean isTA, String createdAt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = active;
        this.isTA = isTA;
        this.createdAt = createdAt;
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

    public User.UserRole getRole() {
        return role;
    }

    public void setRole(User.UserRole role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getIsTA() {
        return isTA;
    }

    public void setIsTA(Boolean isTA) {
        this.isTA = isTA;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

package com.um.labtrack.entity;

import java.time.LocalDateTime;

/**
 * Represents an active authentication session.
 * Stores the currently logged-in user information.
 */
public class AuthSession {
    private User user;
    private LocalDateTime loginTime;
    private boolean authenticated;

    public AuthSession() {
        this.authenticated = false;
    }

    public AuthSession(User user) {
        this.user = user;
        this.loginTime = LocalDateTime.now();
        this.authenticated = true;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void logout() {
        this.user = null;
        this.loginTime = null;
        this.authenticated = false;
    }
}

package com.um.labtrack.service;

import com.um.labtrack.entity.AuthSession;
import com.um.labtrack.entity.User;
import com.um.labtrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for authentication and authorization.
 * Handles user login, session management, and role-based access control.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private AuthSession currentSession;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentSession = new AuthSession();
    }

    /**
     * Authenticate a user by username and password.
     * Uses BCrypt password encoder for secure password verification.
     *
     * @param username The username
     * @param password The plain text password
     * @return true if authentication successful, false otherwise
     */
    public boolean login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Check if user is active
            if (user.getActive() == null || !user.getActive()) {
                return false;
            }
            
            // Verify password using BCrypt
            if (user.getPassword() != null && passwordEncoder.matches(password, user.getPassword())) {
                currentSession = new AuthSession(user);
                return true;
            }
        }
        return false;
    }

    /**
     * Logout the current user.
     */
    public void logout() {
        currentSession.logout();
    }

    /**
     * Get the current authenticated session.
     *
     * @return The current session
     */
    public AuthSession getCurrentSession() {
        return currentSession;
    }

    /**
     * Check if a user is currently authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return currentSession != null && currentSession.isAuthenticated();
    }

    /**
     * Get the current authenticated user.
     *
     * @return The current user, or null if not authenticated
     */
    public User getCurrentUser() {
        if (isAuthenticated()) {
            return currentSession.getUser();
        }
        return null;
    }

    /**
     * Check if the current user has a specific role.
     *
     * @param role The role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(User.UserRole role) {
        User user = getCurrentUser();
        return user != null && user.getRole() == role;
    }

    /**
     * Check if the current user is an administrator.
     *
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return hasRole(User.UserRole.ADMIN);
    }

    /**
     * Check if the current user is a teacher.
     *
     * @return true if user is teacher, false otherwise
     */
    public boolean isTeacher() {
        return hasRole(User.UserRole.TEACHER);
    }

    /**
     * Check if the current user is a teaching assistant.
     * TA is a flag on STUDENT users, not a separate role.
     *
     * @return true if user is a STUDENT with isTA flag set to true, false otherwise
     */
    public boolean isTA() {
        User user = getCurrentUser();
        return user != null && user.getRole() == User.UserRole.STUDENT && user.getIsTA() != null && user.getIsTA();
    }

    /**
     * Check if the current user is a student.
     *
     * @return true if user is student, false otherwise
     */
    public boolean isStudent() {
        return hasRole(User.UserRole.STUDENT);
    }

    /**
     * Sets the current session by username.
     * Used by filters/interceptors to set session from HTTP header.
     *
     * @param username The username to set as current session
     * @return true if user exists and is active, false otherwise
     */
    public boolean setSessionByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check if user is active
            if (user.getActive() != null && user.getActive()) {
                currentSession = new AuthSession(user);
                return true;
            }
        }
        return false;
    }
}

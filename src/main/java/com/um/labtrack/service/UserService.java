package com.um.labtrack.service;

import com.um.labtrack.entity.User;
import com.um.labtrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Role hierarchy rules:
 * - ADMINISTRATOR can create TEACHER
 * - TEACHER can create TA and STUDENT
 * - TA (Teaching Assistant - student with TA privileges) can create STUDENT
 * - STUDENT cannot create anyone
 * - ADMINISTRATOR role cannot be created by anyone (must be manually set in DB)
 */

/**
 * Service layer for User business logic.
 * This service acts as an intermediary between controllers and repositories,
 * encapsulating business rules and transaction management.
 * 
 * The @Service annotation marks this class as a Spring service component,
 * and @Transactional ensures database operations are properly managed.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor-based dependency injection.
     *
     * @param userRepository   The repository for User data access
     * @param authService      The authentication service for role checks
     * @param passwordEncoder  Password encoder for hashing passwords
     */
    @Autowired
    public UserService(UserRepository userRepository, AuthService authService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieve all users from the database.
     *
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Find a user by their ID.
     *
     * @param id The user ID
     * @return Optional containing the User if found
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find a user by their username.
     *
     * @param username The username
     * @return Optional containing the User if found
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Create a new user in the database with role hierarchy enforcement.
     *
     * @param user The user entity to save
     * @return The saved user entity with generated ID
     * @throws IllegalStateException if user is not authenticated
     * @throws IllegalArgumentException if username already exists or role hierarchy violated
     */
    public User createUser(User user) {
        // Check authentication
        if (!authService.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated to create users");
        }

        User currentUser = authService.getCurrentUser();
        User.UserRole newUserRole = user.getRole();

        // Prohibit ADMIN creation (except during initial setup)
        if (newUserRole == User.UserRole.ADMIN) {
            throw new IllegalArgumentException("ADMIN role cannot be created through the application");
        }

        // Enforce role hierarchy
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            // ADMIN can create TEACHER and STUDENT (but not ADMIN)
            if (newUserRole != User.UserRole.TEACHER && newUserRole != User.UserRole.STUDENT) {
                throw new IllegalArgumentException("ADMIN can create TEACHER or STUDENT users");
            }
        } else if (currentUser.getRole() == User.UserRole.TEACHER) {
            // TEACHER can create STUDENT
            if (newUserRole != User.UserRole.STUDENT) {
                throw new IllegalArgumentException("TEACHER can only create STUDENT users");
            }
        } else if (currentUser.getRole() == User.UserRole.STUDENT) {
            // Students cannot create anyone (even if they are TA)
            throw new IllegalArgumentException("STUDENT users cannot create other users");
        }

        // Validate isTA flag: only STUDENT can have isTA = true
        if (user.getIsTA() != null && user.getIsTA() && newUserRole != User.UserRole.STUDENT) {
            throw new IllegalArgumentException("Only STUDENT users can have TA flag set to true");
        }
        
        // If role is STUDENT and isTA is not set, default to false
        if (newUserRole == User.UserRole.STUDENT && user.getIsTA() == null) {
            user.setIsTA(false);
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        // Hash password before saving
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * Update an existing user.
     * ADMIN can update any user, including role changes.
     *
     * @param id   The user ID to update
     * @param user The updated user data
     * @return The updated user entity
     * @throws IllegalArgumentException if user not found
     */
    public User updateUser(Long id, User user) {
        if (!authService.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated to update users");
        }

        User currentUser = authService.getCurrentUser();
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // ADMIN can update everything
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            // ADMIN has full control - no restrictions
        }
        // Other roles can be restricted later if needed
        
        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        // Only update password if explicitly provided and not empty
        // This ensures password is not changed when updating other user fields
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            // Hash password before saving
            existingUser.setPassword(passwordEncoder.encode(user.getPassword().trim()));
        }
        // If password is null or empty, leave the existing password unchanged
        if (user.getRole() != null) {
            // ADMIN can change any role
            if (currentUser.getRole() == User.UserRole.ADMIN) {
                existingUser.setRole(user.getRole());
            }
        }
        if (user.getActive() != null) {
            existingUser.setActive(user.getActive());
        }
        if (user.getIsTA() != null) {
            // Only STUDENT can have isTA flag
            if (existingUser.getRole() == User.UserRole.STUDENT) {
                existingUser.setIsTA(user.getIsTA());
            } else if (user.getIsTA()) {
                throw new IllegalArgumentException("Only STUDENT users can have TA flag set to true");
            }
        }
        
        return userRepository.save(existingUser);
    }

    /**
     * Deactivate a user (soft delete).
     * ADMIN can deactivate any user except themselves.
     *
     * @param id The user ID to deactivate
     * @return The deactivated user
     * @throws IllegalArgumentException if user not found
     * @throws IllegalStateException if trying to deactivate own ADMIN account
     */
    public User deactivateUser(Long id) {
        if (!authService.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated to deactivate users");
        }

        User currentUser = authService.getCurrentUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // ADMIN can deactivate any user, but cannot deactivate themselves
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            if (user.getId().equals(currentUser.getId())) {
                throw new IllegalStateException("ADMIN cannot deactivate their own account");
            }
            // ADMIN can deactivate anyone else
        } else {
            // Non-ADMIN users cannot deactivate ADMIN users
            if (user.getRole() == User.UserRole.ADMIN) {
                throw new IllegalArgumentException("Only ADMIN can deactivate ADMIN users");
            }
        }
        
        user.setActive(false);
        return userRepository.save(user);
    }

    /**
     * Set or unset the TA flag on a STUDENT.
     * ADMIN and TEACHER can perform this action.
     *
     * @param studentId The student ID
     * @param isTA      Whether to set the student as TA
     * @return The updated user
     * @throws IllegalArgumentException if user not found or not a STUDENT
     */
    public User setStudentTAFlag(Long studentId, Boolean isTA) {
        if (!authService.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated");
        }

        User currentUser = authService.getCurrentUser();
        // ADMIN and TEACHER can set TA flag
        if (currentUser.getRole() != User.UserRole.ADMIN && currentUser.getRole() != User.UserRole.TEACHER) {
            throw new IllegalArgumentException("Only ADMIN or TEACHER can set TA flag on students");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + studentId));

        if (student.getRole() != User.UserRole.STUDENT) {
            throw new IllegalArgumentException("User is not a STUDENT and cannot have TA flag set");
        }

        student.setIsTA(isTA != null ? isTA : false);
        return userRepository.save(student);
    }

    /**
     * Promote a STUDENT to TA (sets isTA flag to true).
     * ADMIN and TEACHER can perform this action.
     *
     * @param studentId The student ID to promote
     * @return The updated user with isTA = true
     * @throws IllegalArgumentException if user not found or not a STUDENT
     */
    public User promoteStudentToTA(Long studentId) {
        return setStudentTAFlag(studentId, true);
    }
}

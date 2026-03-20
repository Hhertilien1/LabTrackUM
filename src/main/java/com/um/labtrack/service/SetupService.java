package com.um.labtrack.service;

import com.um.labtrack.entity.User;
import com.um.labtrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for initial setup and bootstrap operations.
 * Handles first-run admin creation and setup state checks.
 */
@Service
@Transactional
public class SetupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private static boolean setupCompleted = false;

    @Autowired
    public SetupService(UserRepository userRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Checks if initial setup is required (no users exist).
     *
     * @return true if setup is required, false otherwise
     */
    public boolean isSetupRequired() {
        if (setupCompleted) {
            return false;
        }
        long userCount = userRepository.count();
        if (userCount == 0) {
            return true;
        } else {
            setupCompleted = true;
            return false;
        }
    }

    /**
     * Creates the initial admin user during first-run setup.
     * This can only be called once when no users exist.
     *
     * @param username Username for admin
     * @param password Password for admin (will be hashed)
     * @param fullName Full name of admin
     * @param email Email of admin
     * @return Created admin user
     * @throws IllegalStateException if setup is not required or already completed
     * @throws IllegalArgumentException if username already exists
     */
    public User createInitialAdmin(String username, String password, String fullName, String email) {
        if (!isSetupRequired()) {
            throw new IllegalStateException("Setup is not required or has already been completed");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        // Hash password using BCrypt
        String hashedPassword = passwordEncoder.encode(password);

        User admin = new User(username, fullName, email, hashedPassword, User.UserRole.ADMIN, false);
        User savedAdmin = userRepository.save(admin);
        
        setupCompleted = true;
        return savedAdmin;
    }

    /**
     * Checks if setup has been completed.
     *
     * @return true if setup is completed, false otherwise
     */
    public boolean isSetupCompleted() {
        return setupCompleted || userRepository.count() > 0;
    }

    /**
     * Get all users with password hashes (for debugging/testing only).
     * WARNING: This exposes password hashes and should only be used for testing.
     *
     * @return List of all users with their password hashes
     */
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, readOnly = true)
    public List<Map<String, Object>> getAllUsersForDebug() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId() != null ? user.getId() : "N/A");
            userMap.put("username", user.getUsername() != null ? user.getUsername() : "N/A");
            userMap.put("fullName", user.getFullName() != null ? user.getFullName() : "N/A");
            userMap.put("email", user.getEmail() != null ? user.getEmail() : "N/A");
            userMap.put("role", user.getRole() != null ? user.getRole().toString() : "N/A");
            userMap.put("active", user.getActive() != null ? user.getActive() : true);
            userMap.put("passwordHash", user.getPassword() != null ? user.getPassword() : "N/A"); // Password hash (BCrypt)
            // Format date as string to avoid serialization issues
            if (user.getCreatedAt() != null) {
                userMap.put("createdAt", user.getCreatedAt().toString());
            } else {
                userMap.put("createdAt", "N/A");
            }
            result.add(userMap);
        }
        
        return result;
    }

    /**
     * Migrate database schema to add missing columns and create missing tables.
     * This is a one-time migration for testing purposes.
     */
    @Transactional
    public void migrateDatabase() {
        try {
            // Check if active column exists in users table
            boolean activeColumnExists = false;
            try {
                jdbcTemplate.queryForObject("SELECT active FROM users LIMIT 1", Boolean.class);
                activeColumnExists = true;
            } catch (Exception e) {
                // Column doesn't exist, we'll add it
            }

            if (!activeColumnExists) {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN active BOOLEAN DEFAULT TRUE");
                // Update existing rows to have active = true
                jdbcTemplate.update("UPDATE users SET active = TRUE WHERE active IS NULL");
            }

            // Check if is_ta column exists in users table
            boolean isTAColumnExists = false;
            try {
                jdbcTemplate.queryForObject("SELECT is_ta FROM users LIMIT 1", Boolean.class);
                isTAColumnExists = true;
            } catch (Exception e) {
                // Column doesn't exist, we'll add it
            }

            if (!isTAColumnExists) {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN is_ta BOOLEAN DEFAULT FALSE");
                // Update existing rows to have is_ta = false
                jdbcTemplate.update("UPDATE users SET is_ta = FALSE WHERE is_ta IS NULL");
            }

            // Create locations table first (equipment references it)
            try {
                jdbcTemplate.queryForObject("SELECT 1 FROM locations LIMIT 1", Integer.class);
            } catch (Exception e) {
                // Table doesn't exist, create it
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS locations (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "building VARCHAR(255) NOT NULL, " +
                    "room VARCHAR(255) NOT NULL, " +
                    "cabinet VARCHAR(255)" +
                    ")");
            }

            // Create equipment table if it doesn't exist (after locations)
            try {
                jdbcTemplate.queryForObject("SELECT 1 FROM equipment LIMIT 1", Integer.class);
            } catch (Exception e) {
                // Table doesn't exist, create it
                // Note: "condition" is a reserved keyword in MySQL, so we use backticks
                // Create table without foreign key first
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS equipment (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "item_number VARCHAR(255) NOT NULL UNIQUE, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "`condition` VARCHAR(50) NOT NULL, " +
                    "location_id BIGINT NULL, " +
                    "status VARCHAR(50) NOT NULL, " +
                    "created_at TIMESTAMP NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                
                // Add foreign key constraint separately if locations table exists
                try {
                    jdbcTemplate.queryForObject("SELECT 1 FROM locations LIMIT 1", Integer.class);
                    // Check if foreign key already exists
                    try {
                        jdbcTemplate.execute("ALTER TABLE equipment ADD CONSTRAINT fk_equipment_location " +
                            "FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL");
                    } catch (Exception fkError) {
                        // Foreign key might already exist, ignore
                    }
                } catch (Exception locError) {
                    // Locations table doesn't exist yet, skip foreign key
                }
            }

            // Create transactions table if it doesn't exist
            try {
                jdbcTemplate.queryForObject("SELECT 1 FROM transactions LIMIT 1", Integer.class);
            } catch (Exception e) {
                // Table doesn't exist, create it
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "equipment_id BIGINT NOT NULL, " +
                    "user_id BIGINT NOT NULL, " +
                    "action VARCHAR(50) NOT NULL, " +
                    "timestamp TIMESTAMP NOT NULL, " +
                    "FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");
            }
        } catch (Exception e) {
            throw new RuntimeException("Migration failed: " + e.getMessage(), e);
        }
    }
}

package com.um.labtrack.config;

import com.um.labtrack.entity.User;
import com.um.labtrack.repository.UserRepository;
import com.um.labtrack.service.SetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Database initializer that runs on application startup.
 * Ensures all required tables exist in the database.
 * Creates default user for easy login if it doesn't exist.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final SetupService setupService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DatabaseInitializer(SetupService setupService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.setupService = setupService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Run migration to ensure all tables exist
            setupService.migrateDatabase();
            System.out.println("Database migration completed successfully");
            
            // Create default user for easy login if it doesn't exist
            createDefaultUser();
        } catch (Exception e) {
            // Log but don't fail startup - Hibernate will create tables with ddl-auto=update
            System.err.println("Database migration check: " + e.getMessage());
            System.out.println("Note: Hibernate will attempt to create tables automatically with ddl-auto=update");
        }
    }

    /**
     * Creates a default user "faisalseraj" with password "Pass@1234" for easy login.
     * Only creates if the user doesn't already exist.
     */
    private void createDefaultUser() {
        try {
            if (!userRepository.existsByUsername("faisalseraj")) {
                String hashedPassword = passwordEncoder.encode("Pass@1234");
                User defaultUser = new User("faisalseraj", "Faisal Seraj", "faisalseraj@example.com", hashedPassword, User.UserRole.ADMIN, false);
                defaultUser.setActive(true);
                userRepository.save(defaultUser);
                System.out.println("Default user 'faisalseraj' created successfully (password: Pass@1234)");
            } else {
                System.out.println("Default user 'faisalseraj' already exists");
            }
        } catch (Exception e) {
            System.err.println("Error creating default user: " + e.getMessage());
        }
    }
}

package com.um.labtrack.config;

import com.um.labtrack.service.SetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Database initializer that runs on application startup.
 * Ensures all required tables exist via migration.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final SetupService setupService;

    @Autowired
    public DatabaseInitializer(SetupService setupService) {
        this.setupService = setupService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            setupService.migrateDatabase();
            System.out.println("Database migration completed successfully");
        } catch (Exception e) {
            System.err.println("Database migration check: " + e.getMessage());
            System.out.println("Note: Hibernate will attempt to create tables automatically with ddl-auto=update");
        }
    }
}

package com.um.labtrack;

import com.um.labtrack.ui.MainFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.swing.*;

/**
 * Application Launcher that starts both the Spring Boot backend server
 * and the Swing UI frontend.
 * 
 * This class extends the Spring Boot application and launches the Swing UI
 * once the backend server is ready.
 */
@SpringBootApplication
public class ApplicationLauncher {

    /**
     * Main entry point for the application.
     * Starts the Spring Boot application context.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system property to use the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start Spring Boot application
        SpringApplication.run(ApplicationLauncher.class, args);
    }

    /**
     * Event listener that launches the Swing UI once the Spring Boot application is ready.
     * This ensures the backend server is fully initialized before the UI tries to connect.
     *
     * @param event The ApplicationReadyEvent fired when Spring Boot is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        // Launch Swing UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            System.out.println("Swing UI launched successfully!");
        });
    }
}

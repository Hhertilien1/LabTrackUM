package com.um.labtrack.ui;

import com.um.labtrack.ui.panels.LoginPanel;
import com.um.labtrack.ui.panels.SetupPanel;
import com.um.labtrack.ui.panels.UserManagementPanel;
import com.um.labtrack.ui.panels.EquipmentManagementPanel;
import com.um.labtrack.ui.panels.LocationManagementPanel;
import com.um.labtrack.ui.panels.TransactionLogPanel;
import com.um.labtrack.ui.util.ApiClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Main Swing UI Frame for the LabTrack UM application.
 * This class creates the primary user interface window with authentication
 * and a tabbed interface containing panels for managing users, equipment, locations, and transactions.
 */
public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private LoginPanel loginPanel;
    private SetupPanel setupPanel;
    private JPanel mainContentPanel;
    private JLabel userInfoLabel;
    private JButton logoutButton;
    
    // Panel references for auto-refresh on tab change
    private UserManagementPanel userManagementPanel;
    private EquipmentManagementPanel equipmentManagementPanel;
    private LocationManagementPanel locationManagementPanel;
    private TransactionLogPanel transactionLogPanel;

    /**
     * Constructor that initializes the UI components and sets up the frame.
     */
    public MainFrame() {
        initializeUI();
        checkSetupStatus();
    }

    /**
     * Initializes all UI components and sets up the frame layout.
     */
    private void initializeUI() {
        setTitle("LabTrack UM - Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setApplicationIcon();

        // Create main content panel
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(new Color(245, 245, 250));

        // Create login panel
        loginPanel = new LoginPanel(success -> {
            if (success) {
                showMainInterface();
            }
        });

        // Create setup panel
        setupPanel = new SetupPanel(success -> {
            if (success) {
                showLogin();
            }
        });
    }

    /**
     * Sets the window/launcher icon from {@code src/main/resources/icon.png}.
     * To change the icon: replace that file with your own PNG (e.g. 32x32 or 256x256).
     * If the file is missing, the default icon is left unchanged.
     */
    private void setApplicationIcon() {
        try (InputStream in = getClass().getResourceAsStream("/icon.png")) {
            if (in != null) {
                BufferedImage img = ImageIO.read(in);
                if (img != null) {
                    setIconImage(img);
                }
            }
        } catch (IOException ignored) {
            // No icon or invalid file — keep default
        }
    }

    /**
     * Checks if initial setup is required and shows appropriate screen.
     */
    private void checkSetupStatus() {
        SwingUtilities.invokeLater(() -> {
            try {
                String response = ApiClient.get("/setup/required");
                boolean setupRequired = response.contains("\"required\":true");
                
                if (setupRequired) {
                    showSetup();
                } else {
                    showLogin();
                }
            } catch (Exception e) {
                // If error, default to login screen
                showLogin();
            }
        });
    }

    /**
     * Shows the setup panel for initial admin creation.
     */
    private void showSetup() {
        getContentPane().removeAll();
        add(setupPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Shows the login panel.
     */
    private void showLogin() {
        getContentPane().removeAll();
        add(loginPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Shows the main interface after successful login.
     */
    private void showMainInterface() {
        getContentPane().removeAll();

        // Create header panel with user info and logout
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(new Color(245, 245, 250));
        
        // Create panel instances
        userManagementPanel = new UserManagementPanel();
        equipmentManagementPanel = new EquipmentManagementPanel();
        locationManagementPanel = new LocationManagementPanel();
        transactionLogPanel = new TransactionLogPanel();
        
        // Add panels to tabs based on user role
        String userRole = getUserRole();
        
        // Store panel references with their tab indices
        int usersTabIndex = -1;
        int equipmentTabIndex = -1;
        int locationsTabIndex = -1;
        int transactionsTabIndex = -1;
        
        // Only show Users tab if user is not a STUDENT
        if (userRole != null && !"STUDENT".equals(userRole)) {
            usersTabIndex = tabbedPane.getTabCount();
            tabbedPane.addTab("Users", createStyledPanel(userManagementPanel));
        }
        
        // All users can access these tabs
        equipmentTabIndex = tabbedPane.getTabCount();
        tabbedPane.addTab("Equipment", createStyledPanel(equipmentManagementPanel));
        
        locationsTabIndex = tabbedPane.getTabCount();
        tabbedPane.addTab("Locations", createStyledPanel(locationManagementPanel));
        
        transactionsTabIndex = tabbedPane.getTabCount();
        tabbedPane.addTab("Transactions", createStyledPanel(transactionLogPanel));

        // Add change listener to refresh data when tab is selected
        final int finalUsersTabIndex = usersTabIndex;
        final int finalEquipmentTabIndex = equipmentTabIndex;
        final int finalLocationsTabIndex = locationsTabIndex;
        final int finalTransactionsTabIndex = transactionsTabIndex;
        
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            
            // Refresh the selected panel based on tab index
            if (selectedIndex == finalUsersTabIndex && userManagementPanel != null) {
                userManagementPanel.refresh();
            } else if (selectedIndex == finalEquipmentTabIndex && equipmentManagementPanel != null) {
                equipmentManagementPanel.refresh();
            } else if (selectedIndex == finalLocationsTabIndex && locationManagementPanel != null) {
                locationManagementPanel.refresh();
            } else if (selectedIndex == finalTransactionsTabIndex && transactionLogPanel != null) {
                transactionLogPanel.refresh();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // Add welcome label
        JLabel welcomeLabel = new JLabel("Welcome to LabTrack UM - TA Lab Inventory System", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(41, 128, 185));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        welcomeLabel.setBackground(new Color(245, 245, 250));
        welcomeLabel.setOpaque(true);
        add(welcomeLabel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    /**
     * Creates a styled wrapper panel for tab content.
     */
    private JPanel createStyledPanel(JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(245, 245, 250));
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Creates the header panel with user info and logout button.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        userInfoLabel = new JLabel("User: Not logged in");
        userInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userInfoLabel.setForeground(Color.WHITE);
        headerPanel.add(userInfoLabel, BorderLayout.WEST);

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setPreferredSize(new Dimension(100, 30));
        logoutButton.addActionListener(e -> {
            try {
                com.um.labtrack.ui.util.ApiClient.post("/auth/logout", "{}");
                // Clear username from ApiClient
                com.um.labtrack.ui.util.ApiClient.clearCurrentUsername();
            } catch (Exception ex) {
                // Ignore logout errors
            }
            showLogin();
        });

        // Button hover effect
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(192, 57, 43));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(231, 76, 60));
            }
        });

        headerPanel.add(logoutButton, BorderLayout.EAST);

        // Update user info
        updateUserInfo();

        return headerPanel;
    }

    /**
     * Updates the user info label from the current session.
     */
    private void updateUserInfo() {
        SwingUtilities.invokeLater(() -> {
            try {
                String response = com.um.labtrack.ui.util.ApiClient.get("/auth/session");
                if (response.contains("\"authenticated\":true")) {
                    // Extract username and role from response
                    String username = extractField(response, "username");
                    String role = extractField(response, "role");
                    if (username != null && role != null) {
                        userInfoLabel.setText("User: " + username + " (" + role + ")");
                    }
                }
            } catch (Exception e) {
                userInfoLabel.setText("User: Not logged in");
            }
        });
    }

    private String extractField(String json, String fieldName) {
        try {
            int start = json.indexOf("\"" + fieldName + "\"");
            if (start == -1) return null;
            start = json.indexOf(":", start) + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
            if (start >= json.length()) return null;
            
            if (json.charAt(start) == '"') {
                start++;
                int end = json.indexOf("\"", start);
                if (end == -1) return null;
                return json.substring(start, end);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Gets the current user's role.
     *
     * @return User role string or null if not authenticated
     */
    private String getUserRole() {
        try {
            String response = ApiClient.get("/auth/session");
            if (response.contains("\"authenticated\":true")) {
                return extractField(response, "role");
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}

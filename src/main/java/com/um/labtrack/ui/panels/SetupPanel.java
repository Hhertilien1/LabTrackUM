package com.um.labtrack.ui.panels;

import com.um.labtrack.ui.util.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Setup Panel for initial admin creation.
 * Shown only during first-run when no users exist in the system.
 */
public class SetupPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JButton createButton;
    private JLabel statusLabel;
    private Consumer<Boolean> onSetupComplete;

    public SetupPanel(Consumer<Boolean> onSetupComplete) {
        this.onSetupComplete = onSetupComplete;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 250));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Title
        JLabel titleLabel = new JLabel("Initial Setup", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Create the first administrator account", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 40, 0);
        add(subtitleLabel, gbc);

        // Setup Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(8, 8, 8, 8);
        formGbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
        Color labelColor = new Color(52, 73, 94);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        usernameLabel.setForeground(labelColor);
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.weightx = 0;
        formPanel.add(usernameLabel, formGbc);

        usernameField = new JTextField(20);
        usernameField.setFont(fieldFont);
        usernameField.setForeground(Color.BLACK);
        usernameField.setBackground(Color.WHITE);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formPanel.add(usernameField, formGbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(labelColor);
        formGbc.gridx = 0;
        formGbc.gridy = 1;
        formGbc.weightx = 0;
        formPanel.add(passwordLabel, formGbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(fieldFont);
        passwordField.setForeground(Color.BLACK);
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formPanel.add(passwordField, formGbc);

        // Confirm Password
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(labelFont);
        confirmPasswordLabel.setForeground(labelColor);
        formGbc.gridx = 0;
        formGbc.gridy = 2;
        formGbc.weightx = 0;
        formPanel.add(confirmPasswordLabel, formGbc);

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(fieldFont);
        confirmPasswordField.setForeground(Color.BLACK);
        confirmPasswordField.setBackground(Color.WHITE);
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formPanel.add(confirmPasswordField, formGbc);

        // Full Name
        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setFont(labelFont);
        fullNameLabel.setForeground(labelColor);
        formGbc.gridx = 0;
        formGbc.gridy = 3;
        formGbc.weightx = 0;
        formPanel.add(fullNameLabel, formGbc);

        fullNameField = new JTextField(20);
        fullNameField.setFont(fieldFont);
        fullNameField.setForeground(Color.BLACK);
        fullNameField.setBackground(Color.WHITE);
        fullNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formPanel.add(fullNameField, formGbc);

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(labelColor);
        formGbc.gridx = 0;
        formGbc.gridy = 4;
        formGbc.weightx = 0;
        formPanel.add(emailLabel, formGbc);

        emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        emailField.setForeground(Color.BLACK);
        emailField.setBackground(Color.WHITE);
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formPanel.add(emailField, formGbc);

        // Create Button
        createButton = new JButton("Create Admin Account");
        createButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        createButton.setBackground(new Color(39, 174, 96));
        createButton.setForeground(Color.WHITE);
        createButton.setBorderPainted(false);
        createButton.setFocusPainted(false);
        createButton.setOpaque(true);
        createButton.setContentAreaFilled(true);
        createButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createButton.setPreferredSize(new Dimension(220, 40));
        createButton.addActionListener(new CreateAdminListener());

        createButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                createButton.setBackground(new Color(46, 204, 113));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                createButton.setBackground(new Color(39, 174, 96));
            }
        });

        formGbc.gridx = 0;
        formGbc.gridy = 5;
        formGbc.gridwidth = 2;
        formGbc.fill = GridBagConstraints.NONE;
        formGbc.anchor = GridBagConstraints.CENTER;
        formGbc.insets = new Insets(20, 8, 8, 8);
        formPanel.add(createButton, formGbc);

        // Status Label
        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(192, 57, 43));
        statusLabel.setOpaque(false);
        formGbc.gridy = 6;
        formGbc.insets = new Insets(10, 8, 0, 8);
        formPanel.add(statusLabel, formGbc);

        // Add form to main panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(formPanel, gbc);
    }

    private class CreateAdminListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();

            // Validation
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
                fullName.isEmpty() || email.isEmpty()) {
                statusLabel.setText("Please fill in all fields");
                statusLabel.setForeground(new Color(192, 57, 43));
                return;
            }

            if (password.length() < 6) {
                statusLabel.setText("Password must be at least 6 characters");
                statusLabel.setForeground(new Color(192, 57, 43));
                return;
            }

            if (!password.equals(confirmPassword)) {
                statusLabel.setText("Passwords do not match");
                statusLabel.setForeground(new Color(192, 57, 43));
                return;
            }

            createButton.setEnabled(false);
            statusLabel.setText("Creating admin account...");
            statusLabel.setForeground(new Color(52, 152, 219));

            SwingUtilities.invokeLater(() -> {
                try {
                    String jsonBody = String.format(
                        "{\"username\":\"%s\",\"password\":\"%s\",\"confirmPassword\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\"}",
                        username, password, confirmPassword, fullName, email
                    );
                    String response = ApiClient.post("/setup/admin", jsonBody);
                    
                    if (response.contains("\"success\":true")) {
                        statusLabel.setText("Admin account created successfully!");
                        statusLabel.setForeground(new Color(39, 174, 96));
                        if (onSetupComplete != null) {
                            onSetupComplete.accept(true);
                        }
                    } else {
                        String errorMsg = extractErrorMessage(response);
                        statusLabel.setText("Error: " + errorMsg);
                        statusLabel.setForeground(new Color(192, 57, 43));
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.setForeground(new Color(192, 57, 43));
                } finally {
                    createButton.setEnabled(true);
                }
            });
        }
    }

    private String extractErrorMessage(String json) {
        try {
            int start = json.indexOf("\"error\":\"");
            if (start != -1) {
                start += 9;
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "Failed to create admin account";
    }
}

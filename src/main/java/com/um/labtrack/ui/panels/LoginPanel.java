package com.um.labtrack.ui.panels;

import com.um.labtrack.ui.util.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Login Panel for user authentication.
 * Provides a styled login interface with username and password fields.
 */
public class LoginPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private Consumer<Boolean> onLoginSuccess;

    public LoginPanel(Consumer<Boolean> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
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
        JLabel titleLabel = new JLabel("LabTrack UM", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Inventory Management System", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 40, 0);
        add(subtitleLabel, gbc);

        // Login Panel
        JPanel loginFormPanel = new JPanel(new GridBagLayout());
        loginFormPanel.setBackground(Color.WHITE);
        loginFormPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(8, 8, 8, 8);
        formGbc.anchor = GridBagConstraints.WEST;

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameLabel.setForeground(new Color(52, 73, 94));
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.weightx = 0;
        loginFormPanel.add(usernameLabel, formGbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameField.setForeground(Color.BLACK);
        usernameField.setBackground(Color.WHITE);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        usernameField.setText("herby");

        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        loginFormPanel.add(usernameField, formGbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordLabel.setForeground(new Color(52, 73, 94));
        formGbc.gridx = 0;
        formGbc.gridy = 1;
        formGbc.weightx = 0;
        loginFormPanel.add(passwordLabel, formGbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setForeground(Color.BLACK);
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        passwordField.setText("pass@1234");

        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        loginFormPanel.add(passwordField, formGbc);

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true);
        loginButton.setContentAreaFilled(true);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.addActionListener(new LoginActionListener());
        
        // Button hover effect
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(52, 152, 219));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(41, 128, 185));
            }
        });

        formGbc.gridx = 0;
        formGbc.gridy = 2;
        formGbc.gridwidth = 2;
        formGbc.fill = GridBagConstraints.NONE;
        formGbc.anchor = GridBagConstraints.CENTER;
        formGbc.insets = new Insets(20, 8, 8, 8);
        loginFormPanel.add(loginButton, formGbc);

        // Status Label
        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(192, 57, 43));
        statusLabel.setOpaque(false);
        formGbc.gridy = 3;
        formGbc.insets = new Insets(10, 8, 0, 8);
        loginFormPanel.add(statusLabel, formGbc);

        // Add login form to main panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(loginFormPanel, gbc);

        // Enter key support
        passwordField.addActionListener(new LoginActionListener());
    }

    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter username and password");
                statusLabel.setForeground(new Color(192, 57, 43));
                return;
            }

            loginButton.setEnabled(false);
            statusLabel.setText("Logging in...");
            statusLabel.setForeground(new Color(52, 152, 219));

            SwingUtilities.invokeLater(() -> {
                try {
                    String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
                    String response = ApiClient.post("/auth/login", jsonBody);
                    
                    if (response.contains("\"success\":true")) {
                        // Set username in ApiClient for subsequent requests
                        ApiClient.setCurrentUsername(username);
                        statusLabel.setText("Login successful!");
                        statusLabel.setForeground(new Color(39, 174, 96));
                        if (onLoginSuccess != null) {
                            onLoginSuccess.accept(true);
                        }
                    } else {
                        statusLabel.setText("Invalid username or password");
                        statusLabel.setForeground(new Color(192, 57, 43));
                        passwordField.setText("");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.setForeground(new Color(192, 57, 43));
                } finally {
                    loginButton.setEnabled(true);
                }
            });
        }
    }
}

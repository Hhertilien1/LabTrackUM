package com.um.labtrack.ui.panels;

import com.um.labtrack.ui.util.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel for managing users in the LabTrack UM system.
 * Provides functionality to list, add, edit, and deactivate users.
 */
public class UserManagementPanel extends JPanel {

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField usernameField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    private JCheckBox isTACheckBox;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton promoteToTAButton;
    private JButton viewUserHistoryButton;
    private JLabel statusLabel;
    private JLabel passwordStrengthLabel;
    private Long selectedUserId;
    private String currentUserRole;

    private JTextField searchField;
    private JComboBox<String> filterRoleCombo;
    private JComboBox<String> filterStatusCombo;
    private JButton filterButton;
    private java.util.List<Object[]> allUserData;

    public UserManagementPanel() {
        initializeUI();
        loadUsers();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table
        String[] columnNames = {"ID", "Username", "Full Name", "Email", "Role", "TA", "Status", "Created At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedUserId = Long.parseLong(tableModel.getValueAt(selectedRow, 0).toString());
                    usernameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    fullNameField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    emailField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    // Clear password field when editing - password should not be updated unless explicitly changed
                    passwordField.setText("");
                    passwordStrengthLabel.setText("");
                    if (tableModel.getColumnCount() > 4) {
                        String role = tableModel.getValueAt(selectedRow, 4) != null ? 
                            tableModel.getValueAt(selectedRow, 4).toString() : "STUDENT";
                        roleCombo.setSelectedItem(role);
                        // Update TA checkbox visibility and state
                        if ("STUDENT".equals(role)) {
                            isTACheckBox.setVisible(true);
                            String taStatus = tableModel.getColumnCount() > 5 && tableModel.getValueAt(selectedRow, 5) != null ?
                                tableModel.getValueAt(selectedRow, 5).toString() : "No";
                            isTACheckBox.setSelected("Yes".equals(taStatus));
                        } else {
                            isTACheckBox.setVisible(false);
                            isTACheckBox.setSelected(false);
                        }
                    }
                } else {
                    // No row selected - reset form to empty
                    clearForm();
                }
                updateButtonStates();
            }
        });
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userTable.setRowHeight(25);
        userTable.setGridColor(new Color(230, 230, 230));
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Users",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(52, 73, 94)
        ));

        // Search & filter panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchPanel.setBackground(new Color(245, 245, 250));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(18);
        searchField.setToolTipText("Search by username, full name, or email");
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Role:"));
        filterRoleCombo = new JComboBox<>(new String[]{"All", "ADMIN", "TEACHER", "STUDENT"});
        searchPanel.add(filterRoleCombo);
        searchPanel.add(new JLabel("Status:"));
        filterStatusCombo = new JComboBox<>(new String[]{"All", "Active", "Inactive"});
        searchPanel.add(filterStatusCombo);
        filterButton = createStyledButton("Filter", new Color(52, 152, 219));
        filterButton.setToolTipText("Apply search and filters");
        filterButton.addActionListener(e -> filterUsers());
        searchPanel.add(filterButton);
        JButton clearFilterButton = createStyledButton("Clear", new Color(149, 165, 166));
        clearFilterButton.setToolTipText("Clear search and filters");
        clearFilterButton.addActionListener(e -> {
            searchField.setText("");
            filterRoleCombo.setSelectedIndex(0);
            filterStatusCombo.setSelectedIndex(0);
            filterUsers();
        });
        searchPanel.add(clearFilterButton);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Search & Filter",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(52, 73, 94)
        ));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Style helper
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
        Color labelColor = new Color(52, 73, 94);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        usernameLabel.setForeground(labelColor);
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.setFont(fieldFont);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setFont(labelFont);
        fullNameLabel.setForeground(labelColor);
        formPanel.add(fullNameLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        fullNameField = new JTextField(20);
        fullNameField.setFont(fieldFont);
        fullNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        formPanel.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(labelColor);
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        formPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel passwordLabel = new JLabel("Password (optional for update):");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(labelColor);
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        passwordField.setFont(fieldFont);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        // Add password strength listener
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updatePasswordStrength();
            }
        });
        formPanel.add(passwordField, gbc);
        
        // Password strength label
        passwordStrengthLabel = new JLabel("");
        passwordStrengthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 8, 8, 8);
        formPanel.add(passwordStrengthLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(labelFont);
        roleLabel.setForeground(labelColor);
        formPanel.add(roleLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        // Role combo will be updated based on current user's role
        roleCombo = new JComboBox<>(new String[]{"TEACHER", "STUDENT"});
        roleCombo.setFont(fieldFont);
        roleCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        updateRoleCombo(); // Set allowed roles based on current user
        formPanel.add(roleCombo, gbc);

        // Is TA Checkbox (only for STUDENT role)
        isTACheckBox = new JCheckBox("Is Teaching Assistant (TA)");
        isTACheckBox.setFont(labelFont);
        isTACheckBox.setForeground(labelColor);
        isTACheckBox.setBackground(Color.WHITE);
        isTACheckBox.setVisible(false); // Hidden by default, shown when STUDENT is selected
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 8, 8, 8);
        formPanel.add(isTACheckBox, gbc);
        
        // Add listener to show/hide TA checkbox based on role selection
        roleCombo.addActionListener(e -> {
            String selectedRole = (String) roleCombo.getSelectedItem();
            isTACheckBox.setVisible("STUDENT".equals(selectedRole));
            if (!"STUDENT".equals(selectedRole)) {
                isTACheckBox.setSelected(false);
            }
        });

        // Buttons with styling (row below TA checkbox so it is not hidden)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        addButton = createStyledButton("Add", new Color(39, 174, 96));
        addButton.setToolTipText("Add new user");
        addButton.addActionListener(e -> addUser());
        
        updateButton = createStyledButton("Update", new Color(52, 152, 219));
        updateButton.setToolTipText("Update selected user");
        updateButton.addActionListener(e -> updateUser());
        
        deleteButton = createStyledButton("Deactivate", new Color(231, 76, 60));
        deleteButton.setToolTipText("Deactivate selected user (they cannot log in; history is kept)");
        deleteButton.addActionListener(e -> deactivateSelectedUser());
        
        refreshButton = createStyledButton("Refresh", new Color(149, 165, 166));
        refreshButton.setToolTipText("Reload user list");
        refreshButton.addActionListener(e -> loadUsers());
        
        JButton clearButton = createStyledButton("Clear", new Color(149, 165, 166));
        clearButton.setToolTipText("Clear all form fields and deselect");
        clearButton.addActionListener(e -> {
            clearForm();
            userTable.clearSelection();
            updateButtonStates();
        });
        
        // Promote/Demote TA button (visible for TEACHER and ADMIN; text toggles by selected student's TA status)
        promoteToTAButton = createStyledButton("Promote to TA", new Color(155, 89, 182), 180);
        promoteToTAButton.setToolTipText("Promote selected student to TA / Demote from TA");
        promoteToTAButton.addActionListener(e -> promoteOrDemoteTA());
        promoteToTAButton.setVisible(false);

        viewUserHistoryButton = createStyledButton("User history", new Color(142, 68, 173));
        viewUserHistoryButton.setToolTipText("View check-in/check-out history for selected user");
        viewUserHistoryButton.addActionListener(e -> showUserTransactionHistory());
        viewUserHistoryButton.setVisible(false);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(promoteToTAButton);
        buttonPanel.add(viewUserHistoryButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(15, 8, 8, 8);
        formPanel.add(buttonPanel, gbc);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(127, 140, 141));
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 8, 0, 8);
        formPanel.add(statusLabel, gbc);

        // Layout
        setBackground(new Color(245, 245, 250));
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);

        updateButtonStates();
    }

    /**
     * Applies search text and role/status filters to the user table.
     */
    private void filterUsers() {
        if (allUserData == null) {
            loadUsers();
            return;
        }
        String searchText = searchField.getText().toLowerCase().trim();
        String roleFilter = (String) filterRoleCombo.getSelectedItem();
        String statusFilter = (String) filterStatusCombo.getSelectedItem();

        tableModel.setRowCount(0);
        for (Object[] row : allUserData) {
            boolean matches = true;
            if (!searchText.isEmpty()) {
                String username = row[1] != null ? row[1].toString().toLowerCase() : "";
                String fullName = row[2] != null ? row[2].toString().toLowerCase() : "";
                String email = row[3] != null ? row[3].toString().toLowerCase() : "";
                if (!username.contains(searchText) && !fullName.contains(searchText) && !email.contains(searchText)) {
                    matches = false;
                }
            }
            if (matches && !"All".equals(roleFilter)) {
                String role = row[4] != null ? row[4].toString() : "";
                if (!roleFilter.equals(role)) matches = false;
            }
            if (matches && !"All".equals(statusFilter)) {
                String status = row[6] != null ? row[6].toString() : "";
                if (!statusFilter.equals(status)) matches = false;
            }
            if (matches) tableModel.addRow(row);
        }
        userTable.clearSelection();
        selectedUserId = null;
        updateButtonStates();
    }

    /**
     * Creates a styled button with hover effects.
     */
    private JButton createStyledButton(String text, Color bgColor) {
        return createStyledButton(text, bgColor, 120);
    }

    /**
     * Creates a styled button with hover effects and custom width (e.g. for "Demote from TA").
     */
    private JButton createStyledButton(String text, Color bgColor, int width) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setPreferredSize(new Dimension(Math.max(width, 85), 35));
        button.setMinimumSize(new Dimension(75, 35));
        
        // Cursor: hand when enabled, default (arrow) when disabled so it's clearly not clickable
        // Hover effect only when enabled
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    Color hoverColor = new Color(
                        Math.min(255, bgColor.getRed() + 20),
                        Math.min(255, bgColor.getGreen() + 20),
                        Math.min(255, bgColor.getBlue() + 20)
                    );
                    button.setBackground(hoverColor);
                } else {
                    button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    /**
     * Enables/disables buttons based on selection and role.
     * Add User: enabled when no user selected. Update/Delete: enabled when a user is selected.
     * Promote/Demote: visible for TEACHER/ADMIN, enabled when a STUDENT is selected; text = Demote if already TA, else Promote.
     */
    private void updateButtonStates() {
        addButton.setEnabled(selectedUserId == null);
        updateButton.setEnabled(selectedUserId != null);
        deleteButton.setEnabled(selectedUserId != null);
        viewUserHistoryButton.setVisible(selectedUserId != null);

        boolean showPromoteDemote = "TEACHER".equals(currentUserRole) || "ADMIN".equals(currentUserRole);
        promoteToTAButton.setVisible(showPromoteDemote);
        if (!showPromoteDemote) {
            promoteToTAButton.setEnabled(false);
            return;
        }
        if (selectedUserId == null) {
            promoteToTAButton.setEnabled(false);
            promoteToTAButton.setText("Promote to TA");
            promoteToTAButton.setToolTipText("Promote selected student to TA");
            return;
        }
        int row = userTable.getSelectedRow();
        if (row < 0) {
            promoteToTAButton.setEnabled(false);
            promoteToTAButton.setText("Promote to TA");
            promoteToTAButton.setToolTipText("Promote selected student to TA");
            return;
        }
        String role = tableModel.getColumnCount() > 4 && tableModel.getValueAt(row, 4) != null
            ? tableModel.getValueAt(row, 4).toString() : "";
        String taStatus = tableModel.getColumnCount() > 5 && tableModel.getValueAt(row, 5) != null
            ? tableModel.getValueAt(row, 5).toString() : "No";
        boolean isStudent = "STUDENT".equals(role);
        promoteToTAButton.setEnabled(isStudent);
        boolean isTA = "Yes".equals(taStatus);
        promoteToTAButton.setText(isTA ? "Demote from TA" : "Promote to TA");
        promoteToTAButton.setToolTipText(isTA ? "Demote this student from TA" : "Promote this student to TA");
    }

    /**
     * Shows check-in/check-out history for the currently selected user.
     */
    private void showUserTransactionHistory() {
        if (selectedUserId == null) return;
        int row = userTable.getSelectedRow();
        String userName = row >= 0 && tableModel.getColumnCount() > 1
            ? tableModel.getValueAt(row, 1).toString() : ("ID " + selectedUserId);
        showTransactionHistoryDialog("Check-in/out history: " + userName, "/transactions?userId=" + selectedUserId);
    }

    private void showTransactionHistoryDialog(String title, String path) {
        try {
            String response = ApiClient.get(path);
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);
            String[] colNames = {"ID", "Timestamp", "Action", "Equipment", "Item #", "User"};
            DefaultTableModel model = new DefaultTableModel(colNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable table = new JTable(model);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            table.setRowHeight(24);
            JScrollPane scroll = new JScrollPane(table);
            dialog.add(scroll, BorderLayout.CENTER);
            parseTransactionsIntoTable(response, model);
            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(e -> dialog.dispose());
            JPanel south = new JPanel();
            south.add(closeBtn);
            dialog.add(south, BorderLayout.SOUTH);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void parseTransactionsIntoTable(String json, DefaultTableModel model) {
        model.setRowCount(0);
        if (json == null || json.trim().isEmpty() || !json.contains("{")) return;
        String normalized = json.trim();
        if (normalized.startsWith("[")) normalized = normalized.substring(1);
        if (normalized.endsWith("]")) normalized = normalized.substring(0, normalized.length() - 1);
        String[] items = normalized.split("\\},\\s*\\{");
        for (String raw : items) {
            String item = raw.trim();
            if (item.startsWith("{")) item = item.substring(1);
            if (item.endsWith("}")) item = item.substring(0, item.length() - 1);
            item = "{" + item + "}";
            String id = extractField(item, "id");
            String timestamp = extractField(item, "timestamp");
            String action = extractField(item, "action");
            String equipmentName = extractNestedField(item, "equipment", "name");
            String itemNumber = extractNestedField(item, "equipment", "itemNumber");
            String username = extractNestedField(item, "user", "username");
            if (id != null && !id.isEmpty()) {
                model.addRow(new Object[]{
                    id, timestamp != null ? timestamp : "", action != null ? action : "",
                    equipmentName != null ? equipmentName : "", itemNumber != null ? itemNumber : "",
                    username != null ? username : ""
                });
            }
        }
    }

    private String extractNestedField(String json, String objectName, String fieldName) {
        int objStart = json.indexOf("\"" + objectName + "\"");
        if (objStart == -1) return null;
        int brace = json.indexOf("{", objStart);
        if (brace == -1) return null;
        int objEnd = findMatchingBrace(json, brace);
        if (objEnd == -1) return null;
        return extractField(json.substring(brace, objEnd + 1), fieldName);
    }

    private int findMatchingBrace(String s, int openIndex) {
        int depth = 1;
        for (int i = openIndex + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    /**
     * Updates the role combo box based on current user's role.
     * ADMIN can create TEACHER and STUDENT
     * TEACHER can create STUDENT
     * STUDENT (even with TA flag) cannot create users
     */
    private void updateRoleCombo() {
        try {
            String response = ApiClient.get("/auth/session");
            if (response.contains("\"authenticated\":true")) {
                String role = extractField(response, "role");
                roleCombo.removeAllItems();
                if ("ADMIN".equals(role)) {
                    roleCombo.addItem("TEACHER");
                    roleCombo.addItem("STUDENT");
                } else if ("TEACHER".equals(role)) {
                    roleCombo.addItem("STUDENT");
                }
                // STUDENT cannot create users, so combo stays empty
            }
        } catch (Exception e) {
            // If not authenticated or error, default to STUDENT only
            roleCombo.removeAllItems();
            roleCombo.addItem("STUDENT");
        }
    }

    /**
     * Gets the current user's role (ADMIN, TEACHER, or STUDENT).
     * Promote/Demote button visibility is updated in updateButtonStates().
     */
    private void loadCurrentUserRole() {
        try {
            String response = ApiClient.get("/auth/session");
            if (response.contains("\"authenticated\":true")) {
                currentUserRole = extractField(response, "role");
            } else {
                currentUserRole = null;
            }
        } catch (Exception e) {
            currentUserRole = null;
        }
    }

    private void loadUsers() {
        SwingUtilities.invokeLater(() -> {
            try {
                loadCurrentUserRole(); // Load role first
                statusLabel.setText("Loading users...");
                statusLabel.setForeground(new Color(52, 152, 219));
                String response = ApiClient.get("/users");
                updateTable(response);
                userTable.clearSelection();
                selectedUserId = null;
                updateRoleCombo();
                updateButtonStates();
                statusLabel.setText("");
                statusLabel.setForeground(new Color(39, 174, 96));
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(new Color(231, 76, 60));
                JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Public method to refresh user data.
     * Called automatically when the panel is selected.
     */
    public void refresh() {
        loadUsers();
    }

    private void updateTable(String jsonResponse) {
        tableModel.setRowCount(0);
        allUserData = new java.util.ArrayList<>();
        String[] users = jsonResponse.split("\\},\\{");
        for (String userStr : users) {
            String id = extractField(userStr, "id");
            String username = extractField(userStr, "username");
            String fullName = extractField(userStr, "fullName");
            String email = extractField(userStr, "email");
            String role = extractField(userStr, "role");
            String isTA = extractField(userStr, "isTA");
            String active = extractField(userStr, "active");
            String createdAt = extractField(userStr, "createdAt");
            if (id != null && username != null) {
                String statusText = "true".equalsIgnoreCase(active) ? "Active" : "Inactive";
                String taText = "true".equalsIgnoreCase(isTA) ? "Yes" : "No";
                Object[] row = new Object[]{
                    id, username,
                    fullName != null ? fullName : "",
                    email != null ? email : "",
                    role != null ? role : "STUDENT",
                    taText,
                    statusText,
                    createdAt != null ? createdAt : ""
                };
                allUserData.add(row);
                tableModel.addRow(row);
            }
        }
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
            } else {
                int end = start;
                while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
                return json.substring(start, end).trim();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void addUser() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();
        Boolean isTA = isTACheckBox.isVisible() && isTACheckBox.isSelected();

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields including password", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (role == null || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a role", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate isTA: only STUDENT can have isTA = true
        if (isTA && !"STUDENT".equals(role)) {
            JOptionPane.showMessageDialog(this, "Only STUDENT users can have TA flag set", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Adding user...");
                statusLabel.setForeground(new Color(52, 152, 219));
                String jsonBody = String.format("{\"username\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\",\"isTA\":%s}", 
                    username, fullName, email, password, role, isTA);
                ApiClient.post("/users", jsonBody);
                clearForm();
                loadUsers();
                statusLabel.setText("User added successfully");
                statusLabel.setForeground(new Color(39, 174, 96));
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                if (errorMsg.contains("ADMINISTRATOR")) {
                    errorMsg = "ADMINISTRATOR role cannot be created";
                } else if (errorMsg.contains("can only create")) {
                    errorMsg = "You don't have permission to create this role";
                }
                statusLabel.setText("Error: " + errorMsg);
                statusLabel.setForeground(new Color(231, 76, 60));
                JOptionPane.showMessageDialog(this, "Error adding user: " + errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void updateUser() {
        if (selectedUserId == null) {
            JOptionPane.showMessageDialog(this, "Please select a user to update", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        Boolean isTA = isTACheckBox.isVisible() && isTACheckBox.isSelected();

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Updating user...");
                statusLabel.setForeground(new Color(52, 152, 219));
                // Password is optional for updates - only include if explicitly provided
                // If password field is empty, do not include it in the update request
                String password = new String(passwordField.getPassword()).trim();
                String jsonBody;
                
                // Build JSON body - only include password if it's not empty
                if (!password.isEmpty()) {
                    // User wants to change password
                    jsonBody = String.format("{\"username\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\",\"isTA\":%s}", 
                        username, fullName, email, password, role, isTA);
                } else {
                    // No password change - exclude password from update
                    jsonBody = String.format("{\"username\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"isTA\":%s}", 
                        username, fullName, email, role, isTA);
                }
                
                ApiClient.put("/users/" + selectedUserId, jsonBody);
                clearForm();
                loadUsers();
                statusLabel.setText("User updated successfully");
                statusLabel.setForeground(new Color(39, 174, 96));
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(new Color(231, 76, 60));
                JOptionPane.showMessageDialog(this, "Error updating user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void deactivateSelectedUser() {
        if (selectedUserId == null) {
            JOptionPane.showMessageDialog(this, "Please select a user to deactivate", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Deactivate this user? They will not be able to log in. Check-in/out history is kept.",
            "Confirm deactivate",
            JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Deactivating user...");
                statusLabel.setForeground(new Color(52, 152, 219));
                ApiClient.delete("/users/" + selectedUserId);
                clearForm();
                loadUsers();
                statusLabel.setText("User deactivated successfully");
                statusLabel.setForeground(new Color(39, 174, 96));
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(new Color(231, 76, 60));
                JOptionPane.showMessageDialog(this, "Error deactivating user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Updates password strength indicator based on password field content.
     */
    private void updatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        if (password.isEmpty()) {
            passwordStrengthLabel.setText("");
            return;
        }
        
        int strength = calculatePasswordStrength(password);
        String strengthText;
        Color strengthColor;
        
        if (strength < 2) {
            strengthText = "Weak - Use at least 6 characters";
            strengthColor = new Color(231, 76, 60); // Red
        } else if (strength < 4) {
            strengthText = "Medium - Add numbers or special characters";
            strengthColor = new Color(243, 156, 18); // Orange
        } else {
            strengthText = "Strong password";
            strengthColor = new Color(39, 174, 96); // Green
        }
        
        passwordStrengthLabel.setText(strengthText);
        passwordStrengthLabel.setForeground(strengthColor);
    }

    /**
     * Calculates password strength (0-5).
     * 
     * @param password Password to evaluate
     * @return Strength score (0-5)
     */
    private int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 6) strength++;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength++;
        return strength;
    }

    /**
     * Promotes or demotes the selected STUDENT to/from TA based on current TA status.
     * Only available for TEACHER and ADMIN.
     */
    private void promoteOrDemoteTA() {
        if (selectedUserId == null) {
            JOptionPane.showMessageDialog(this, "Please select a STUDENT", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) return;
        String role = tableModel.getValueAt(selectedRow, 4) != null ? tableModel.getValueAt(selectedRow, 4).toString() : "";
        if (!"STUDENT".equals(role)) {
            JOptionPane.showMessageDialog(this, "Only STUDENT users can be promoted or demoted from TA", "Invalid Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String taStatus = tableModel.getColumnCount() > 5 && tableModel.getValueAt(selectedRow, 5) != null
            ? tableModel.getValueAt(selectedRow, 5).toString() : "No";
        boolean isCurrentlyTA = "Yes".equals(taStatus);
        String action = isCurrentlyTA ? "demote" : "promote";
        String confirmMsg = isCurrentlyTA
            ? "Are you sure you want to demote this student from TA?"
            : "Are you sure you want to promote this STUDENT to TA?";
        int confirm = JOptionPane.showConfirmDialog(this, confirmMsg, isCurrentlyTA ? "Confirm Demote" : "Confirm Promotion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText(isCurrentlyTA ? "Demoting from TA..." : "Promoting user to TA...");
                statusLabel.setForeground(new Color(52, 152, 219));
                if (isCurrentlyTA) {
                    ApiClient.post("/users/" + selectedUserId + "/demote-from-ta", "{}");
                    statusLabel.setText("User demoted from TA successfully");
                } else {
                    ApiClient.post("/users/" + selectedUserId + "/promote-to-ta", "{}");
                    statusLabel.setText("User promoted to TA successfully");
                }
                statusLabel.setForeground(new Color(39, 174, 96));
                clearForm();
                loadUsers();
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                if (errorMsg != null) {
                    if (errorMsg.contains("Only") && errorMsg.contains("TEACHER")) errorMsg = "Only TEACHER or ADMIN can change TA status";
                    else if (errorMsg.contains("not a STUDENT")) errorMsg = "Only STUDENT users can be promoted or demoted from TA";
                }
                statusLabel.setText("Error: " + (errorMsg != null ? errorMsg : "Operation failed"));
                statusLabel.setForeground(new Color(231, 76, 60));
                JOptionPane.showMessageDialog(this, "Error " + action + "ing: " + (errorMsg != null ? errorMsg : e.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void clearForm() {
        usernameField.setText("");
        fullNameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        passwordStrengthLabel.setText("");
        if (roleCombo.getItemCount() > 0) {
            roleCombo.setSelectedIndex(0);
        }
        isTACheckBox.setSelected(false);
        isTACheckBox.setVisible("STUDENT".equals(roleCombo.getSelectedItem()));
        selectedUserId = null;
    }
}

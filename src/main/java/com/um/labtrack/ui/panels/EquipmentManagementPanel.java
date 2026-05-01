package com.um.labtrack.ui.panels;

import com.um.labtrack.ui.util.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel for managing equipment in the LabTrack UM system.
 * Provides functionality to list, add, edit, delete equipment, and perform check-in/check-out operations.
 */
public class EquipmentManagementPanel extends JPanel {

    private JTable equipmentTable;
    private DefaultTableModel tableModel;
    private JTextField itemNumberField;
    private JTextField nameField;
    private JComboBox<String> conditionCombo;
    private JComboBox<String> locationCombo;
    private JLabel assignToStudentLabel;
    private JComboBox<String> userCombo;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton checkoutButton;
    private JButton checkinButton;
    private JButton refreshButton;
    private JLabel statusLabel;
    private Long selectedEquipmentId;
    private String selectedEquipmentStatus; // "AVAILABLE" or "CHECKED_OUT"
    private java.util.List<Object[]> locations;
    private java.util.List<Object[]> users;

    private JButton viewEquipmentHistoryButton;

    // Search/Filter components
    private JTextField searchField;
    private JComboBox<String> filterStatusCombo;
    private JComboBox<String> filterConditionCombo;
    private JButton searchButton;
    private java.util.List<Object[]> allEquipmentData;

    /** Admin, Teacher, or TA may add/update/delete equipment; plain students may only view and use check-in/out if permitted by role. */
    private boolean canManageLocationsAndEquipment;

    public EquipmentManagementPanel() {
        canManageLocationsAndEquipment = ApiClient.fetchCanManageLocationsAndEquipment();
        initializeUI();
        loadLocations();
        loadUsers();
        loadEquipment();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table
        String[] columnNames = {"ID", "Item Number", "Name", "Condition", "Status", "Location"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        equipmentTable = new JTable(tableModel);
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        equipmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = equipmentTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedEquipmentId = Long.parseLong(tableModel.getValueAt(selectedRow, 0).toString());
                    itemNumberField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    nameField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    conditionCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
                    selectedEquipmentStatus = tableModel.getColumnCount() > 4 && tableModel.getValueAt(selectedRow, 4) != null
                        ? tableModel.getValueAt(selectedRow, 4).toString() : "AVAILABLE";
                } else {
                    selectedEquipmentId = null;
                    selectedEquipmentStatus = null;
                }
                updateButtonStates();
            }
        });
        JScrollPane scrollPane = new JScrollPane(equipmentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Equipment"));

        // Create search/filter panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchPanel.add(searchField);
        
        searchPanel.add(new JLabel("Status:"));
        filterStatusCombo = new JComboBox<>(new String[]{"All", "AVAILABLE", "CHECKED_OUT"});
        searchPanel.add(filterStatusCombo);
        
        searchPanel.add(new JLabel("Condition:"));
        filterConditionCombo = new JComboBox<>(new String[]{"All", "FUNCTIONAL", "BROKEN", "IN_REPAIR"});
        searchPanel.add(filterConditionCombo);
        
        searchButton = createStyledButton("Filter", new Color(52, 152, 219));
        searchButton.setToolTipText("Search and filter equipment");
        searchButton.addActionListener(e -> filterEquipment());
        searchPanel.add(searchButton);
        
        JButton clearFilterButton = createStyledButton("Clear", new Color(149, 165, 166));
        clearFilterButton.setToolTipText("Clear search and filters");
        clearFilterButton.addActionListener(e -> {
            searchField.setText("");
            filterStatusCombo.setSelectedIndex(0);
            filterConditionCombo.setSelectedIndex(0);
            filterEquipment();
        });
        searchPanel.add(clearFilterButton);
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Item Number:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        itemNumberField = new JTextField(20);
        formPanel.add(itemNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Condition:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        conditionCombo = new JComboBox<>(new String[]{"FUNCTIONAL", "BROKEN", "IN_REPAIR"});
        formPanel.add(conditionCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        locationCombo = new JComboBox<>();
        formPanel.add(locationCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        assignToStudentLabel = new JLabel("Assign to student (for checkout):");
        assignToStudentLabel.setToolTipText("Select the student who will receive the equipment. Only Admin, Teacher, or TA can check out; equipment is assigned to a student.");
        formPanel.add(assignToStudentLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        userCombo = new JComboBox<>();
        userCombo.setToolTipText("Select the student to assign equipment to. Required for Check Out. Check In collects from whoever has it.");
        userCombo.addActionListener(e -> updateButtonStates());
        formPanel.add(userCombo, gbc);
        assignToStudentLabel.setVisible(false);
        userCombo.setVisible(false);

        // Equipment history button (user history is in User Management panel)
        JPanel historyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        viewEquipmentHistoryButton = createStyledButton("Item history", new Color(142, 68, 173));
        viewEquipmentHistoryButton.setToolTipText("View check-in/check-out history for this equipment");
        viewEquipmentHistoryButton.addActionListener(ev -> showEquipmentTransactionHistory());
        historyPanel.add(viewEquipmentHistoryButton);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(historyPanel, gbc);

        // Buttons with styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        addButton = createStyledButton("Add", new Color(39, 174, 96));
        addButton.setToolTipText("Add new equipment");
        addButton.addActionListener(e -> addEquipment());
        
        updateButton = createStyledButton("Update", new Color(52, 152, 219));
        updateButton.setToolTipText("Update selected equipment");
        updateButton.addActionListener(e -> updateEquipment());
        
        deleteButton = createStyledButton("Delete", new Color(231, 76, 60));
        deleteButton.setToolTipText("Delete selected equipment");
        deleteButton.addActionListener(e -> deleteEquipment());
        
        checkoutButton = createStyledButton("Check Out", new Color(241, 196, 15));
        checkoutButton.setToolTipText("Check out equipment to selected user");
        checkoutButton.addActionListener(e -> checkoutEquipment());
        
        checkinButton = createStyledButton("Check In", new Color(46, 204, 113));
        checkinButton.setToolTipText("Check in equipment (collect from the student who has it)");
        checkinButton.addActionListener(e -> checkinEquipment());
        
        JButton clearButton = createStyledButton("Clear", new Color(149, 165, 166));
        clearButton.setToolTipText("Clear all form fields and deselect");
        clearButton.addActionListener(e -> clearForm());
        
        refreshButton = createStyledButton("Refresh", new Color(149, 165, 166));
        refreshButton.setToolTipText("Reload equipment list");
        refreshButton.addActionListener(e -> {
            loadEquipment();
            loadLocations();
            loadUsers();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(checkoutButton);
        buttonPanel.add(checkinButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(buttonPanel, gbc);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.GRAY);
        gbc.gridy = 7;
        formPanel.add(statusLabel, gbc);

        updateButtonStates();

        formPanel.setBorder(BorderFactory.createTitledBorder("Equipment Form"));

        // Layout
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Enables/disables buttons based on selection and status.
     * - Add: enabled when no equipment selected (new item is just added to system; no check-in required).
     * - Update/Delete: enabled when equipment selected.
     * - Assign to student: visible only when an equipment item is selected (for checkout).
     * - Check Out: enabled when equipment selected, student selected, and status is AVAILABLE.
     * - Check In: enabled when equipment selected and status is CHECKED_OUT.
     * - View equipment history: enabled when equipment is selected.
     */
    private void updateButtonStates() {
        boolean hasEquipment = selectedEquipmentId != null;
        boolean hasUser = userCombo.getSelectedIndex() >= 0 && users != null && !users.isEmpty();
        boolean isAvailable = "AVAILABLE".equals(selectedEquipmentStatus);
        boolean isCheckedOut = "CHECKED_OUT".equals(selectedEquipmentStatus);

        assignToStudentLabel.setVisible(hasEquipment);
        userCombo.setVisible(hasEquipment);

        boolean manage = canManageLocationsAndEquipment;
        addButton.setEnabled(!hasEquipment && manage);
        updateButton.setEnabled(hasEquipment && manage);
        deleteButton.setEnabled(hasEquipment && manage);
        itemNumberField.setEditable(manage);
        nameField.setEditable(manage);
        conditionCombo.setEnabled(manage);
        locationCombo.setEnabled(manage);
        checkoutButton.setEnabled(hasEquipment && hasUser && isAvailable);
        checkinButton.setEnabled(hasEquipment && isCheckedOut);
        viewEquipmentHistoryButton.setEnabled(hasEquipment);
    }

    /**
     * Clears all form fields and deselects the table selection.
     */
    private void clearForm() {
        itemNumberField.setText("");
        nameField.setText("");
        if (conditionCombo.getItemCount() > 0) conditionCombo.setSelectedIndex(0);
        if (locationCombo.getItemCount() > 0) locationCombo.setSelectedIndex(0);
        if (userCombo.getItemCount() > 0) userCombo.setSelectedIndex(0);
        selectedEquipmentId = null;
        selectedEquipmentStatus = null;
        equipmentTable.clearSelection();
        updateButtonStates();
    }

    /**
     * Shows a dialog with check-in/check-out history for the currently selected equipment.
     */
    private void showEquipmentTransactionHistory() {
        if (selectedEquipmentId == null) return;
        String name = nameField.getText();
        String title = "Equipment history: " + (name != null && !name.isEmpty() ? name : "ID " + selectedEquipmentId);
        showTransactionHistoryDialog(title, "/transactions?equipmentId=" + selectedEquipmentId);
    }

    /**
     * Fetches transactions from the API and shows them in a modal dialog.
     */
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

    /**
     * Parses JSON array of transactions and fills the table model.
     * Handles nested equipment and user objects.
     */
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
                    id,
                    timestamp != null ? timestamp : "",
                    action != null ? action : "",
                    equipmentName != null ? equipmentName : "",
                    itemNumber != null ? itemNumber : "",
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
     * Filters equipment based on search text, status, and condition.
     */
    private void filterEquipment() {
        if (allEquipmentData == null) {
            loadEquipment();
            return;
        }
        
        String searchText = searchField.getText().toLowerCase().trim();
        String statusFilter = (String) filterStatusCombo.getSelectedItem();
        String conditionFilter = (String) filterConditionCombo.getSelectedItem();
        
        tableModel.setRowCount(0);
        
        for (Object[] row : allEquipmentData) {
            boolean matches = true;
            
            // Search filter
            if (!searchText.isEmpty()) {
                String itemNumber = row[1] != null ? row[1].toString().toLowerCase() : "";
                String name = row[2] != null ? row[2].toString().toLowerCase() : "";
                if (!itemNumber.contains(searchText) && !name.contains(searchText)) {
                    matches = false;
                }
            }
            
            // Status filter
            if (matches && !"All".equals(statusFilter)) {
                String status = row[4] != null ? row[4].toString() : "";
                if (!statusFilter.equals(status)) {
                    matches = false;
                }
            }
            
            // Condition filter
            if (matches && !"All".equals(conditionFilter)) {
                String condition = row[3] != null ? row[3].toString() : "";
                if (!conditionFilter.equals(condition)) {
                    matches = false;
                }
            }
            
            if (matches) {
                tableModel.addRow(row);
            }
        }
    }

    private void loadEquipment() {
        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Loading equipment...");
                statusLabel.setForeground(Color.BLUE);
                String response = ApiClient.get("/equipment");
                updateTable(response);
                equipmentTable.clearSelection();
                selectedEquipmentId = null;
                selectedEquipmentStatus = null;
                updateButtonStates();
                statusLabel.setText("");
                statusLabel.setForeground(Color.GREEN);
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error loading equipment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Public method to refresh equipment data.
     * Called automatically when the panel is selected.
     */
    public void refresh() {
        canManageLocationsAndEquipment = ApiClient.fetchCanManageLocationsAndEquipment();
        updateButtonStates();
        loadEquipment();
        loadLocations();
        loadUsers();
    }

    private void loadLocations() {
        SwingUtilities.invokeLater(() -> {
            try {
                String response = ApiClient.get("/locations");
                locationCombo.removeAllItems();
                locations = parseLocations(response);
                for (Object[] loc : locations) {
                    locationCombo.addItem(loc[1].toString() + " - " + loc[2].toString() + (loc[3] != null ? " - " + loc[3].toString() : ""));
                }
            } catch (Exception e) {
                // Silently fail - locations might not be available yet
            }
        });
    }

    private void loadUsers() {
        SwingUtilities.invokeLater(() -> {
            try {
                String response = ApiClient.get("/users");
                userCombo.removeAllItems();
                java.util.List<Object[]> allUsers = parseUsers(response);
                // Only students can be assigned equipment; dropdown and list match by index
                users = new java.util.ArrayList<>();
                for (Object[] user : allUsers) {
                    String role = user.length > 3 && user[3] != null ? user[3].toString() : "";
                    if (!"STUDENT".equals(role)) continue;
                    userCombo.addItem(user[1].toString() + " (" + user[2].toString() + ")");
                    users.add(user);
                }
            } catch (Exception e) {
                // Silently fail - users might not be available yet
            }
        });
    }

    private java.util.List<Object[]> parseLocations(String json) {
        java.util.List<Object[]> result = new java.util.ArrayList<>();
        String[] locations = json.split("\\},\\{");
        for (String locStr : locations) {
            String id = extractField(locStr, "id");
            String building = extractField(locStr, "building");
            String room = extractField(locStr, "room");
            String cabinet = extractField(locStr, "cabinet");
            if (id != null) {
                result.add(new Object[]{id, building, room, cabinet});
            }
        }
        return result;
    }

    private java.util.List<Object[]> parseUsers(String json) {
        java.util.List<Object[]> result = new java.util.ArrayList<>();
        String[] users = json.split("\\},\\{");
        for (String userStr : users) {
            String id = extractField(userStr, "id");
            String username = extractField(userStr, "username");
            String fullName = extractField(userStr, "fullName");
            String role = extractField(userStr, "role");
            String active = extractField(userStr, "active");
            // Only include active users for checkout/checkin
            if (id != null && (active == null || "true".equals(active))) {
                result.add(new Object[]{id, username, fullName, role});
            }
        }
        return result;
    }

    private void updateTable(String jsonResponse) {
        tableModel.setRowCount(0);
        allEquipmentData = new java.util.ArrayList<>();
        String[] equipment = jsonResponse.split("\\},\\{");
        for (String eqStr : equipment) {
            String id = extractField(eqStr, "id");
            String itemNumber = extractField(eqStr, "itemNumber");
            String name = extractField(eqStr, "name");
            String condition = extractField(eqStr, "condition");
            String status = extractField(eqStr, "status");
            String location = extractField(eqStr, "location");
            if (id != null && itemNumber != null) {
                Object[] row = new Object[]{
                    id, itemNumber, name != null ? name : "", 
                    condition != null ? condition : "", 
                    status != null ? status : "",
                    location != null ? location : ""
                };
                allEquipmentData.add(row);
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

    private void addEquipment() {
        String itemNumber = itemNumberField.getText().trim();
        String name = nameField.getText().trim();
        String condition = (String) conditionCombo.getSelectedItem();

        if (itemNumber.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in item number and name", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Adding equipment...");
                statusLabel.setForeground(Color.BLUE);
                String locationId = locationCombo.getSelectedIndex() >= 0 && locations != null ? locations.get(locationCombo.getSelectedIndex())[0].toString() : "null";
                String jsonBody = String.format("{\"itemNumber\":\"%s\",\"name\":\"%s\",\"condition\":\"%s\",\"location\":%s}", 
                    itemNumber, name, condition, locationId.equals("null") ? "null" : "{\"id\":" + locationId + "}");
                ApiClient.post("/equipment", jsonBody);
                clearForm();
                loadEquipment();
                statusLabel.setText("Equipment added successfully");
                statusLabel.setForeground(Color.GREEN);
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error adding equipment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void updateEquipment() {
        if (selectedEquipmentId == null) {
            JOptionPane.showMessageDialog(this, "Please select equipment to update", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String itemNumber = itemNumberField.getText().trim();
        String name = nameField.getText().trim();
        String condition = (String) conditionCombo.getSelectedItem();

        if (itemNumber.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in item number and name", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Updating equipment...");
                statusLabel.setForeground(Color.BLUE);
                String locationId = locationCombo.getSelectedIndex() >= 0 && locations != null ? locations.get(locationCombo.getSelectedIndex())[0].toString() : "null";
                String jsonBody = String.format("{\"itemNumber\":\"%s\",\"name\":\"%s\",\"condition\":\"%s\",\"location\":%s}", 
                    itemNumber, name, condition, locationId.equals("null") ? "null" : "{\"id\":" + locationId + "}");
                ApiClient.put("/equipment/" + selectedEquipmentId, jsonBody);
                clearForm();
                loadEquipment();
                statusLabel.setText("Equipment updated successfully");
                statusLabel.setForeground(Color.GREEN);
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error updating equipment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void deleteEquipment() {
        if (selectedEquipmentId == null) {
            JOptionPane.showMessageDialog(this, "Please select equipment to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this equipment?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Deleting equipment...");
                statusLabel.setForeground(Color.BLUE);
                ApiClient.delete("/equipment/" + selectedEquipmentId);
                clearForm();
                loadEquipment();
                statusLabel.setText("Equipment deleted successfully");
                statusLabel.setForeground(Color.GREEN);
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error deleting equipment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void checkoutEquipment() {
        if (selectedEquipmentId == null) {
            JOptionPane.showMessageDialog(this, "Please select equipment to check out", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userCombo.getSelectedIndex() < 0 || users == null) {
            JOptionPane.showMessageDialog(this, "Please select a user", "No User Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Checking out equipment...");
                statusLabel.setForeground(Color.BLUE);
                String userId = users.get(userCombo.getSelectedIndex())[0].toString();
                String jsonBody = "{\"userId\":" + userId + "}";
                ApiClient.post("/equipment/" + selectedEquipmentId + "/checkout", jsonBody);
                loadEquipment();
                statusLabel.setText("Equipment checked out successfully");
                statusLabel.setForeground(Color.GREEN);
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error checking out equipment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void checkinEquipment() {
        if (selectedEquipmentId == null) {
            JOptionPane.showMessageDialog(this, "Please select equipment to check in", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Checking in equipment...");
                statusLabel.setForeground(Color.BLUE);
                // No userId needed: backend collects from whoever currently has the equipment
                ApiClient.post("/equipment/" + selectedEquipmentId + "/checkin", "{}");
                loadEquipment();
                statusLabel.setText("Equipment checked in successfully");
                statusLabel.setForeground(Color.GREEN);
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error checking in equipment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Creates a styled button with proper colors and visibility.
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setPreferredSize(new Dimension(100, 35));
        button.setMinimumSize(new Dimension(80, 35));
        
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
}

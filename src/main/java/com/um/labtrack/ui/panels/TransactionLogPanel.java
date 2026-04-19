package com.um.labtrack.ui.panels;

import com.um.labtrack.ui.util.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for displaying and managing transaction logs in the LabTrack UM system.
 * Shows all check-in and check-out operations with timestamps.
 * Provides search, filter, and management capabilities.
 */
public class TransactionLogPanel extends JPanel {

    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton refreshButton;
    private JButton clearFilterButton;
    private JLabel statusLabel;
    
        // Filter components
    private JTextField searchField;
    private JComboBox<String> filterActionCombo;
    private JComboBox<String> filterUserCombo;
    private JComboBox<String> filterEquipmentCombo;
    
    // Data storage for filtering
    private List<Object[]> allTransactionData;
    private java.util.List<Object[]> users;
    private java.util.List<Object[]> equipment;

    public TransactionLogPanel() {
        initializeUI();
        loadUsers();
        loadEquipment();
        loadTransactions();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 245, 250));

        // Create table with enhanced columns (no equipment type / item number)
        String[] columnNames = {"ID", "Timestamp", "Equipment", "User", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionTable.setRowHeight(25);
        transactionTable.setGridColor(new Color(230, 230, 230));
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Enable sorting
        sorter = new TableRowSorter<>(tableModel);
        transactionTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Transaction Log",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(52, 73, 94)
        ));

        // Create search/filter panel
        JPanel searchPanel = createSearchFilterPanel();

        // Create control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controlPanel.setBackground(Color.WHITE);
        
        refreshButton = createStyledButton("Refresh", new Color(149, 165, 166));
        refreshButton.setToolTipText("Reload transactions");
        refreshButton.addActionListener(e -> {
            loadUsers();
            loadEquipment();
            loadTransactions();
        });
        controlPanel.add(refreshButton);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(127, 140, 141));
        controlPanel.add(statusLabel);

        // Layout
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the search and filter panel.
     */
    private JPanel createSearchFilterPanel() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 12);
        Color labelColor = new Color(52, 73, 94);
        
        // Search field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(labelFont);
        searchLabel.setForeground(labelColor);
        searchPanel.add(searchLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        searchField.addActionListener(e -> filterTransactions());
        searchPanel.add(searchField, gbc);
        
        // Action filter
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel actionLabel = new JLabel("Action:");
        actionLabel.setFont(labelFont);
        actionLabel.setForeground(labelColor);
        searchPanel.add(actionLabel, gbc);
        
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        filterActionCombo = new JComboBox<>(new String[]{"All", "CHECKIN", "CHECKOUT"});
        filterActionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterActionCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        filterActionCombo.addActionListener(e -> filterTransactions());
        searchPanel.add(filterActionCombo, gbc);
        
        // User filter
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel userLabel = new JLabel("User:");
        userLabel.setFont(labelFont);
        userLabel.setForeground(labelColor);
        searchPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        filterUserCombo = new JComboBox<>(new String[]{"All"});
        filterUserCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterUserCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        filterUserCombo.addActionListener(e -> filterTransactions());
        searchPanel.add(filterUserCombo, gbc);
        
        // Equipment filter
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel equipmentLabel = new JLabel("Equipment:");
        equipmentLabel.setFont(labelFont);
        equipmentLabel.setForeground(labelColor);
        searchPanel.add(equipmentLabel, gbc);
        
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        filterEquipmentCombo = new JComboBox<>(new String[]{"All"});
        filterEquipmentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterEquipmentCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        filterEquipmentCombo.addActionListener(e -> filterTransactions());
        searchPanel.add(filterEquipmentCombo, gbc);
        
        // Filter and Clear buttons
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton filterButton = createStyledButton("Filter", new Color(52, 152, 219));
        filterButton.setToolTipText("Apply filters to transactions");
        filterButton.addActionListener(e -> filterTransactions());
        buttonPanel.add(filterButton);
        
        clearFilterButton = createStyledButton("Clear", new Color(149, 165, 166));
        clearFilterButton.setToolTipText("Clear filters");
        clearFilterButton.addActionListener(e -> clearFilters());
        buttonPanel.add(clearFilterButton);
        
        searchPanel.add(buttonPanel, gbc);
        
        return searchPanel;
    }

    private void loadTransactions() {
        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Loading transactions...");
                statusLabel.setForeground(Color.BLUE);
                String response = ApiClient.get("/transactions");
                updateTable(response);
                statusLabel.setText("");
                statusLabel.setForeground(Color.GREEN);
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Public method to refresh transaction data.
     * Called automatically when the panel is selected.
     */
    public void refresh() {
        loadTransactions();
        loadUsers();
        loadEquipment();
    }

    private void updateTable(String jsonResponse) {
        tableModel.setRowCount(0);
        allTransactionData = new ArrayList<>();
        
        String[] transactions = jsonResponse.split("\\},\\{");
        for (String transStr : transactions) {
            String id = extractField(transStr, "id");
            String equipment = extractField(transStr, "equipment");
            String user = extractField(transStr, "user");
            String action = extractField(transStr, "action");
            String timestamp = extractField(transStr, "timestamp");
            
            // Extract equipment details from nested object
            String equipmentName = extractNestedField(equipment, "name");
            String itemNumber = extractNestedField(equipment, "itemNumber");
            if (equipmentName == null) equipmentName = itemNumber;
            if (itemNumber == null) itemNumber = extractNestedField(equipment, "id");
            
            // Extract user details from nested object
            String userName = extractNestedField(user, "username");
            if (userName == null) userName = extractNestedField(user, "fullName");
            if (userName == null) userName = extractNestedField(user, "id");
            
            // Format timestamp
            String formattedTimestamp = formatTimestamp(timestamp);
            
            if (id != null) {
                Object[] rowData = new Object[]{
                    id,
                    formattedTimestamp != null ? formattedTimestamp : (timestamp != null ? timestamp : ""),
                    equipmentName != null ? equipmentName : "N/A",
                    userName != null ? userName : "N/A",
                    action != null ? action : ""
                };
                allTransactionData.add(rowData);
                tableModel.addRow(rowData);
            }
        }
    }
    
    /**
     * Formats timestamp string for better readability.
     */
    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            // If timestamp is in ISO format, try to format it
            if (timestamp.contains("T")) {
                // Format: 2024-01-31T14:30:00 -> 2024-01-31 14:30:00
                return timestamp.replace("T", " ").substring(0, Math.min(timestamp.length(), 19));
            }
            return timestamp;
        } catch (Exception e) {
            return timestamp;
        }
    }
    
    /**
     * Filters transactions based on search criteria.
     */
    private void filterTransactions() {
        if (allTransactionData == null || allTransactionData.isEmpty()) {
            return;
        }
        
        tableModel.setRowCount(0);
        
        String searchText = searchField.getText().trim().toLowerCase();
        String actionFilter = (String) filterActionCombo.getSelectedItem();
        String userFilter = (String) filterUserCombo.getSelectedItem();
        String equipmentFilter = (String) filterEquipmentCombo.getSelectedItem();
        
        for (Object[] row : allTransactionData) {
            boolean matches = true;
            
            // Search filter (searches in all text fields)
            if (!searchText.isEmpty()) {
                boolean searchMatch = false;
                for (Object cell : row) {
                    if (cell != null && cell.toString().toLowerCase().contains(searchText)) {
                        searchMatch = true;
                        break;
                    }
                }
                if (!searchMatch) matches = false;
            }
            
            // Action filter (null-safe: combos can be null when cleared/repopulated)
            if (matches && actionFilter != null && !"All".equals(actionFilter)) {
                String action = row[4] != null ? row[4].toString() : "";
                if (!actionFilter.equals(action)) {
                    matches = false;
                }
            }
            
            // User filter
            if (matches && userFilter != null && !"All".equals(userFilter)) {
                String user = row[3] != null ? row[3].toString() : "";
                if (!userFilter.equals(user)) {
                    matches = false;
                }
            }
            
            // Equipment filter
            if (matches && equipmentFilter != null && !"All".equals(equipmentFilter)) {
                String equipment = row[2] != null ? row[2].toString() : "";
                if (!equipmentFilter.equals(equipment)) {
                    matches = false;
                }
            }
            
            if (matches) {
                tableModel.addRow(row);
            }
        }
        
        statusLabel.setText("Showing " + tableModel.getRowCount() + " transaction(s)");
        statusLabel.setForeground(new Color(52, 152, 219));
    }
    
    /**
     * Loads users for the filter dropdown. Only students are listed, since transactions are for student check-in/check-out.
     */
    private void loadUsers() {
        SwingUtilities.invokeLater(() -> {
            try {
                String response = ApiClient.get("/users");
                users = parseUsers(response);
                filterUserCombo.removeAllItems();
                filterUserCombo.addItem("All");
                if (users != null) {
                    for (Object[] user : users) {
                        String role = user.length > 3 && user[3] != null ? user[3].toString() : "";
                        if (!"STUDENT".equals(role)) continue;
                        String username = user[1] != null ? user[1].toString() : "";
                        filterUserCombo.addItem(username);
                    }
                }
            } catch (Exception e) {
                // Silently fail - users might not be available
            }
        });
    }
    
    /**
     * Loads equipment for the filter dropdown.
     */
    private void loadEquipment() {
        SwingUtilities.invokeLater(() -> {
            try {
                String response = ApiClient.get("/equipment");
                equipment = parseEquipment(response);
                filterEquipmentCombo.removeAllItems();
                filterEquipmentCombo.addItem("All");
                if (equipment != null) {
                    for (Object[] eq : equipment) {
                        String name = eq[2] != null ? eq[2].toString() : "";
                        filterEquipmentCombo.addItem(name);
                    }
                }
            } catch (Exception e) {
                // Silently fail - equipment might not be available
            }
        });
    }
    
    private java.util.List<Object[]> parseUsers(String jsonResponse) {
        List<Object[]> userList = new ArrayList<>();
        if (jsonResponse == null || jsonResponse.trim().isEmpty() || !jsonResponse.contains("{")) return userList;
        String[] users = jsonResponse.split("\\},\\{");
        for (String userStr : users) {
            String id = extractField(userStr, "id");
            String username = extractField(userStr, "username");
            String fullName = extractField(userStr, "fullName");
            String role = extractField(userStr, "role");
            if (id != null && username != null) {
                userList.add(new Object[]{id, username, fullName, role});
            }
        }
        return userList;
    }
    
    private java.util.List<Object[]> parseEquipment(String jsonResponse) {
        List<Object[]> equipmentList = new ArrayList<>();
        if (jsonResponse == null || jsonResponse.trim().isEmpty() || !jsonResponse.contains("{")) return equipmentList;
        String[] equipment = jsonResponse.split("\\},\\{");
        for (String eqStr : equipment) {
            String id = extractField(eqStr, "id");
            String itemNumber = extractField(eqStr, "itemNumber");
            String name = extractField(eqStr, "name");
            if (id != null && name != null) {
                equipmentList.add(new Object[]{id, itemNumber, name});
            }
        }
        return equipmentList;
    }
    
    /**
     * Clears all filters and shows all transactions.
     */
    private void clearFilters() {
        searchField.setText("");
        filterActionCombo.setSelectedIndex(0);
        filterUserCombo.setSelectedIndex(0);
        filterEquipmentCombo.setSelectedIndex(0);
        
        // Reload all transactions
        if (allTransactionData != null) {
            tableModel.setRowCount(0);
            for (Object[] row : allTransactionData) {
                tableModel.addRow(row);
            }
        }
        
        statusLabel.setText("All filters cleared");
        statusLabel.setForeground(new Color(127, 140, 141));
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
            } else if (json.charAt(start) == '{') {
                // Nested object - return the whole object as string
                int depth = 1;
                int end = start + 1;
                while (end < json.length() && depth > 0) {
                    if (json.charAt(end) == '{') depth++;
                    if (json.charAt(end) == '}') depth--;
                    end++;
                }
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

    private String extractNestedField(String nestedJson, String fieldName) {
        if (nestedJson == null || nestedJson.isEmpty()) return null;
        return extractField(nestedJson, fieldName);
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

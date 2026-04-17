package com.um.labtrack.ui.panels;

import com.um.labtrack.ui.util.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel for managing locations in the LabTrack UM system.
 * Provides functionality to list and add locations (building, room, cabinet).
 */
public class LocationManagementPanel extends JPanel {

    private JTable locationTable;
    private DefaultTableModel tableModel;
    private JTextField buildingField;
    private JTextField roomField;
    private JTextField cabinetField;
    private JButton addButton;
    private JButton refreshButton;
    private JLabel statusLabel;

    public LocationManagementPanel() {
        initializeUI();
        loadLocations();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table
        String[] columnNames = {"ID", "Building", "Room", "Cabinet"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        locationTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(locationTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Locations"));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Building:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        buildingField = new JTextField(20);
        formPanel.add(buildingField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        roomField = new JTextField(20);
        formPanel.add(roomField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Cabinet (optional):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        cabinetField = new JTextField(20);
        formPanel.add(cabinetField, gbc);

        // Buttons with styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        addButton = createStyledButton("Add", new Color(39, 174, 96));
        addButton.setToolTipText("Add new location");
        addButton.addActionListener(e -> addLocation());
        
        JButton clearButton = createStyledButton("Clear", new Color(149, 165, 166));
        clearButton.setToolTipText("Clear all form fields");
        clearButton.addActionListener(e -> {
            buildingField.setText("");
            roomField.setText("");
            cabinetField.setText("");
        });
        
        refreshButton = createStyledButton("Refresh", new Color(149, 165, 166));
        refreshButton.setToolTipText("Reload locations");
        refreshButton.addActionListener(e -> loadLocations());

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(buttonPanel, gbc);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.GRAY);
        gbc.gridy = 4;
        formPanel.add(statusLabel, gbc);

        formPanel.setBorder(BorderFactory.createTitledBorder("Location Form"));

        // Layout
        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);
    }

    private void loadLocations() {
        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Loading locations...");
                statusLabel.setForeground(Color.BLUE);
                String response = ApiClient.get("/locations");
                updateTable(response);
                statusLabel.setText("");
                statusLabel.setForeground(Color.GREEN);
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error loading locations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Public method to refresh location data.
     * Called automatically when the panel is selected.
     */
    public void refresh() {
        loadLocations();
    }

    private void updateTable(String jsonResponse) {
        tableModel.setRowCount(0);
        String[] locations = jsonResponse.split("\\},\\{");
        for (String locStr : locations) {
            String id = extractField(locStr, "id");
            String building = extractField(locStr, "building");
            String room = extractField(locStr, "room");
            String cabinet = extractField(locStr, "cabinet");
            if (id != null && building != null) {
                tableModel.addRow(new Object[]{
                    id, building, room != null ? room : "", cabinet != null ? cabinet : ""
                });
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

    private void addLocation() {
        String building = buildingField.getText().trim();
        String room = roomField.getText().trim();
        String cabinet = cabinetField.getText().trim();

        if (building.isEmpty() || room.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in building and room", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Adding location...");
                statusLabel.setForeground(Color.BLUE);
                String jsonBody;
                if (cabinet.isEmpty()) {
                    jsonBody = String.format("{\"building\":\"%s\",\"room\":\"%s\"}", building, room);
                } else {
                    jsonBody = String.format("{\"building\":\"%s\",\"room\":\"%s\",\"cabinet\":\"%s\"}", building, room, cabinet);
                }
                ApiClient.post("/locations", jsonBody);
                clearForm();
                loadLocations();
                statusLabel.setText("Location added successfully");
                statusLabel.setForeground(Color.GREEN);
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error adding location: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void clearForm() {
        buildingField.setText("");
        roomField.setText("");
        cabinetField.setText("");
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

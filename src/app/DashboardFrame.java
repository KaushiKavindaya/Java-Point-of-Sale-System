package app;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * The main dashboard window for the POS system.
 * This frame acts as the central hub for navigating to different modules.
 * It is responsible for creating and managing the shared data models and the visibility of other windows.
 */
public class DashboardFrame extends JFrame {

    private final Inventory inventory;
    private final SalesLogger salesLogger;

    public DashboardFrame() {
        this.inventory = new Inventory();
        this.salesLogger = new SalesLogger();

        setTitle("POS System - Main Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("POS Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 30, 30));

        JButton posButton = createDashboardButton("Point of Sale", "/pos.png");
        JButton inventoryButton = createDashboardButton("Inventory Management", "/inventory.png");
        JButton reportsButton = createDashboardButton("Sales Reports", "/reports.png");

        buttonPanel.add(posButton);
        buttonPanel.add(inventoryButton);
        buttonPanel.add(reportsButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);

        // --- Action Listeners for Navigation ---
        posButton.addActionListener(e -> {
            PosFrame posFrame = new PosFrame(inventory, salesLogger);
            posFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    DashboardFrame.this.setVisible(true);
                }
            });
            posFrame.setVisible(true);
            this.setVisible(false);
        });

        inventoryButton.addActionListener(e -> {
            this.setVisible(false);
            InventoryDialog inventoryDialog = new InventoryDialog(this, inventory);
            inventoryDialog.setVisible(true);
            this.setVisible(true);
        });

        reportsButton.addActionListener(e -> {
            SalesReportFrame reportFrame = new SalesReportFrame(inventory);
            reportFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    DashboardFrame.this.setVisible(true);
                }
            });
            reportFrame.setVisible(true);
            this.setVisible(false);
        });
    }

    /**
     * A helper method to create and style a dashboard button with an icon.
     */
    private JButton createDashboardButton(String text, String iconPath) {
        JButton button = new JButton(text);
        URL iconUrl = getClass().getResource(iconPath);

        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            Image img = icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        } else {
            System.err.println("Couldn't find dashboard icon: " + iconPath);
        }

        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}
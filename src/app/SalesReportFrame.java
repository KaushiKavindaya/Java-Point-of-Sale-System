package app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SalesReportFrame extends JFrame {

    private final Inventory inventory;
    private JTable salesTable;
    private DefaultTableModel salesTableModel;
    private JTable saleItemsTable;
    private DefaultTableModel saleItemsTableModel;

    public SalesReportFrame(Inventory inventory) {
        this.inventory = inventory;

        setTitle("Sales Report");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Master Table (All Sales) ---
        salesTableModel = new DefaultTableModel(new String[]{"Sale ID", "Date", "Total Price", "Payment Method"}, 0);
        salesTable = new JTable(salesTableModel);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- Detail Table (Items for Selected Sale) ---
        saleItemsTableModel = new DefaultTableModel(new String[]{"Product", "Quantity", "Price at Sale", "Subtotal"}, 0);
        saleItemsTable = new JTable(saleItemsTableModel);

        // --- Split Pane to hold both tables ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(salesTable),
                new JScrollPane(saleItemsTable));
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        // --- Bottom Panel for Back Button ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("⬅ Back to Dashboard");
        backButton.addActionListener(e -> this.dispose());
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);

        // --- Add Listener to the Master Table ---
        salesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = salesTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Get the Sale ID from the selected row (it's in the first column)
                    int saleId = (int) salesTable.getValueAt(selectedRow, 0);
                    loadSaleItems(saleId);
                }
            }
        });

        // Load initial data
        loadSales();
    }

    private void loadSales() {
        salesTableModel.setRowCount(0); // Clear existing data
        List<Sale> sales = inventory.getAllSales();
        for (Sale sale : sales) {
            salesTableModel.addRow(new Object[]{
                    sale.getId(),
                    sale.getFormattedDate(),
                    String.format("%.2f", sale.getTotalPrice()),
                    sale.getPaymentMethod()
            });
        }
    }

    private void loadSaleItems(int saleId) {
        saleItemsTableModel.setRowCount(0); // Clear existing data
        List<SaleItem> items = inventory.getSaleItems(saleId);
        for (SaleItem item : items) {
            saleItemsTableModel.addRow(new Object[]{
                    item.getProductName(),
                    item.getQuantity(),
                    String.format("%.2f", item.getPriceAtSale()),
                    String.format("%.2f", item.getSubtotal())
            });
        }
    }
}
package app;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.net.URL;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PosFrame extends JFrame {

    // --- Data Models & State ---
    private final Inventory inventory;
    private final SalesLogger salesLogger;
    private final Map<Product, Integer> currentCart;
    private final Map<String, ImageIcon> imageCache = new HashMap<>();
    private boolean isProgrammaticChange = false;

    // --- GUI Components ---
    private JList<Object> categoryList;
    private DefaultListModel<Object> categoryListModel;
    private JTextField searchField;
    private JPanel productGridPanel;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JLabel totalLabel;

    private final String ALL_PRODUCTS_CATEGORY = "All Products";

    public PosFrame(Inventory inventory, SalesLogger salesLogger) {
        this.inventory = inventory;
        this.salesLogger = salesLogger;
        this.currentCart = new HashMap<>();

        setTitle("Point of Sale");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createProductSelectionPanel(), createCartPanel());
        mainSplit.setDividerLocation(600); // Narrower product panel, wider cart
        add(mainSplit, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("⬅ Back to Dashboard");
        backButton.addActionListener(e -> this.dispose());
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);

        loadCategories();
        displayProducts();
    }

    private JPanel createProductSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createCategoryPanel(), createProductGridAndSearchPanel());
        split.setDividerLocation(200);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Categories"));
        categoryListModel = new DefaultListModel<>();
        categoryList = new JList<>(categoryListModel);
        categoryList.setFont(new Font("Arial", Font.BOLD, 16));
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || isProgrammaticChange) return;
            isProgrammaticChange = true;
            searchField.setText("");
            isProgrammaticChange = false;
            displayProducts();
        });
        panel.add(new JScrollPane(categoryList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProductGridAndSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void handleSearchChange() {
                if (isProgrammaticChange) return;
                isProgrammaticChange = true;
                if (categoryList.getSelectedIndex() != 0) categoryList.setSelectedIndex(0);
                isProgrammaticChange = false;
                displayProducts();
            }
            public void insertUpdate(DocumentEvent e) { handleSearchChange(); }
            public void removeUpdate(DocumentEvent e) { handleSearchChange(); }
            public void changedUpdate(DocumentEvent e) { handleSearchChange(); }
        });
        panel.add(searchField, BorderLayout.NORTH);

        productGridPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        JScrollPane scrollPane = new JScrollPane(productGridPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Cart"));

        String[] columnNames = {"Image", "Item", "Ref #", "Qty", "Price", "Subtotal", "Remove"};
        cartTableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return column == 3 || column == 6; }
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return ImageIcon.class;
                return Object.class;
            }
        };

        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(60);
        cartTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        cartTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(6).setPreferredWidth(80);

        cartTable.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer());
        cartTable.getColumnModel().getColumn(3).setCellEditor(new QuantityEditor());
        cartTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        cartTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor());

        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel checkoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total: Rs.0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutButton.addActionListener(e -> handleCheckout());
        checkoutPanel.add(totalLabel);
        checkoutPanel.add(checkoutButton);
        bottomPanel.add(checkoutPanel, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadCategories() {
        categoryListModel.clear();
        categoryListModel.addElement(ALL_PRODUCTS_CATEGORY);
        inventory.getAllCategories().forEach(categoryListModel::addElement);
        categoryList.setSelectedIndex(0);
    }

    private void displayProducts() {
        productGridPanel.removeAll();
        String searchTerm = searchField.getText();
        Object selectedCategoryObj = categoryList.getSelectedValue();
        Category category = (selectedCategoryObj instanceof Category) ? (Category) selectedCategoryObj : null;
        inventory.searchProducts(searchTerm, category).forEach(p -> productGridPanel.add(createProductCard(p)));
        productGridPanel.revalidate();
        productGridPanel.repaint();
    }

    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createEtchedBorder());
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String imageFilename = (product.getImagePath() != null && !product.getImagePath().isEmpty()) ? product.getImagePath() : "no_image_specified.png";
        String resourcePath = "/images/" + imageFilename;

        System.out.println("Attempting to load image resource: " + resourcePath);

        URL imageUrl = getClass().getResource(resourcePath);

        JLabel imageLabel = new JLabel();
        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(imageUrl);
            Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(image));
        } else {
            imageLabel.setText("No Image");
            System.err.println("ERROR: Could not find image at path: " + resourcePath);
        }
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(imageLabel, BorderLayout.CENTER);

        String labelText = String.format("<html><center>%s<br>Rs.%.2f</center></html>", product.getName(), product.getPrice());
        JLabel nameLabel = new JLabel(labelText);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(nameLabel, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            Border defaultBorder = card.getBorder(), hoverBorder = BorderFactory.createLineBorder(Color.BLUE, 2);
            public void mouseClicked(MouseEvent e) { handleAddToCart(product); }
            public void mouseEntered(MouseEvent e) { card.setBorder(hoverBorder); }
            public void mouseExited(MouseEvent e) { card.setBorder(defaultBorder); }
        });
        return card;
    }

    private void handleAddToCart(Product product) {
        int stock = inventory.getStockCount(product);
        int inCart = currentCart.getOrDefault(product, 0);
        if (inCart + 1 > stock) {
            JOptionPane.showMessageDialog(this, "Not enough stock for " + product.getName(), "Stock Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        currentCart.put(product, inCart + 1);
        updateCartView();
    }

    private void handleCheckout() {
        if (currentCart.isEmpty()) { JOptionPane.showMessageDialog(this, "Cart is empty!", "Error", JOptionPane.ERROR_MESSAGE); return; }

        BigDecimal finalTotal = currentCart.entrySet().stream()
                .map(entry -> BigDecimal.valueOf(entry.getKey().getPrice()).multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CheckoutDialog dialog = new CheckoutDialog(this, finalTotal.doubleValue());
        dialog.setVisible(true);

        if (dialog.isPaymentSuccessful()) {
            String paymentMethod = dialog.getPaymentMethod();
            BigDecimal cashTendered = dialog.getCashTendered();
            BigDecimal changeGiven = dialog.getChangeGiven();
            String cardType = dialog.getCardType();

            salesLogger.logSale(currentCart, finalTotal, paymentMethod, cashTendered, changeGiven, cardType);
            currentCart.forEach((product, qty) -> inventory.reduceStock(product, qty));

            JOptionPane.showMessageDialog(this, "Payment Successful! " + totalLabel.getText(), "Success", JOptionPane.INFORMATION_MESSAGE);

            currentCart.clear();
            updateCartView();
            displayProducts();
        } else {
            System.out.println("Checkout canceled by user.");
        }
    }

    private void updateCartView() {
        if (cartTable.isEditing()) cartTable.getCellEditor().stopCellEditing();

        cartTableModel.setRowCount(0);
        for (Map.Entry<Product, Integer> entry : currentCart.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            double subtotal = p.getPrice() * qty;
            cartTableModel.addRow(new Object[]{ p, p.getName(), p.getRefNumber(), qty, String.format("%.2f", p.getPrice()), String.format("%.2f", subtotal), "Remove" });
        }
        recalculateGrandTotal();
    }

    private void recalculateGrandTotal() {
        double total = currentCart.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue()).sum();
        totalLabel.setText(String.format("Total: Rs.%.2f", total));
    }

    // --- INNER CLASSES FOR CUSTOM JTABLE ---
    private class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel();
            if (value instanceof Product p) {
                String imagePath = p.getImagePath();
                if (imagePath != null && !imagePath.isEmpty()) {
                    if (imageCache.containsKey(imagePath)) {
                        label.setIcon(imageCache.get(imagePath));
                    } else {
                        URL url = getClass().getResource("/images/" + imagePath);
                        if (url != null) {
                            ImageIcon icon = new ImageIcon(url);
                            Image scaled = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            ImageIcon scaledIcon = new ImageIcon(scaled);
                            imageCache.put(imagePath, scaledIcon);
                            label.setIcon(scaledIcon);
                        }
                    }
                }
            }
            label.setHorizontalAlignment(CENTER);
            return label;
        }
    }

    private class QuantityEditor extends AbstractCellEditor implements TableCellEditor {
        final JSpinner spinner;
        Product currentProduct;
        JTable table;
        int row;

        QuantityEditor() {
            spinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
            spinner.addChangeListener(e -> SwingUtilities.invokeLater(this::updateTotalsFromSpinner));
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;
            this.currentProduct = (Product) table.getValueAt(row, 0);
            ((SpinnerNumberModel) spinner.getModel()).setMaximum(inventory.getStockCount(currentProduct));
            spinner.setValue(value);
            return spinner;
        }

        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        private void updateTotalsFromSpinner() {
            if (currentProduct != null) {
                int newQuantity = (Integer) spinner.getValue();
                currentCart.put(currentProduct, newQuantity);
                table.getModel().setValueAt(newQuantity, row, 3);
                double newSubtotal = currentProduct.getPrice() * newQuantity;
                table.getModel().setValueAt(String.format("%.2f", newSubtotal), row, 5);
                recalculateGrandTotal();
            }
        }

        @Override
        public boolean stopCellEditing() {
            updateTotalsFromSpinner();
            return super.stopCellEditing();
        }
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            URL iconUrl = getClass().getResource("/icons/delete.png");
            if (iconUrl != null) {
                setIcon(new ImageIcon(iconUrl));
                setToolTipText("Remove Item");
                setText("");
            } else {
                setText("X");
            }
            return this;
        }
    }

    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        final JButton button = new JButton();
        int row;

        ButtonEditor() {
            button.setOpaque(true);
            button.addActionListener(this);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            URL iconUrl = getClass().getResource("/icons/delete.png");
            if (iconUrl != null) {
                button.setIcon(new ImageIcon(iconUrl));
                button.setToolTipText("Remove Item");
            } else {
                button.setText("X");
            }
            return button;
        }

        public Object getCellEditorValue() { return "Remove"; }

        public void actionPerformed(ActionEvent e) {
            Product productToRemove = (Product) cartTable.getValueAt(row, 0);
            if (productToRemove != null) {
                currentCart.remove(productToRemove);
                updateCartView();
            }
            fireEditingStopped();
        }

        public boolean isCellEditable(EventObject anEvent) { return true; }
    }
}
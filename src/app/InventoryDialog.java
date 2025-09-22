package app;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class InventoryDialog extends JDialog {

    private final Inventory inventory;

    // UI Components
    private JList<Category> categoryList;
    private DefaultListModel<Category> categoryListModel;
    private JList<Product> productList;
    private DefaultListModel<Product> productListModel;

    // Form Components
    private JTextField nameField, priceField, refField, brandField, imagePathField;
    private JSpinner quantitySpinner;
    private JComboBox<Category> categoryComboBox;
    private JLabel imagePreviewLabel;

    public InventoryDialog(Frame owner, Inventory inventory) {
        super(owner, "Inventory Management", true);
        this.inventory = inventory;

        setSize(1200, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createSelectionPanel(), createFormPanel());
        mainSplit.setDividerLocation(350);
        add(mainSplit, BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.SOUTH);

        loadCategories();
    }

    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        categoryListModel = new DefaultListModel<>();
        categoryList = new JList<>(categoryListModel);
        categoryList.setBorder(BorderFactory.createTitledBorder("Categories"));
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        productListModel = new DefaultListModel<>();
        productList = new JList<>(productListModel);
        productList.setCellRenderer(new ProductListCellRenderer());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(categoryList), new JScrollPane(productList));
        split.setDividerLocation(200);
        panel.add(split, BorderLayout.CENTER);

        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadProductsForSelectedCategory();
        });

        productList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm(productList.getSelectedValue());
        });

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        nameField = new JTextField(25);
        refField = new JTextField(15);
        brandField = new JTextField(15);
        priceField = new JTextField(10);
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        categoryComboBox = new JComboBox<>();
        imagePathField = new JTextField(20);
        imagePathField.setEditable(false);
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> handleBrowseImage());
        imagePreviewLabel = new JLabel("No Image Preview");
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setBorder(BorderFactory.createEtchedBorder());
        imagePreviewLabel.setPreferredSize(new Dimension(150, 150));

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(nameField, gbc);

        gbc.gridy = ++y; gbc.gridx = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Ref #:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(refField, gbc);

        gbc.gridy = ++y; gbc.gridx = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Brand:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(brandField, gbc);

        gbc.gridy = ++y; gbc.gridx = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(categoryComboBox, gbc);

        JPanel priceQtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        priceQtyPanel.add(priceField);
        priceQtyPanel.add(Box.createHorizontalStrut(20));
        priceQtyPanel.add(new JLabel("Quantity:"));
        priceQtyPanel.add(Box.createHorizontalStrut(5));
        priceQtyPanel.add(quantitySpinner);

        gbc.gridy = ++y; gbc.gridx = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(priceQtyPanel, gbc);

        JPanel imageFilePanel = new JPanel(new BorderLayout(5, 0));
        imageFilePanel.add(imagePathField, BorderLayout.CENTER);
        imageFilePanel.add(browseButton, BorderLayout.EAST);

        gbc.gridy = ++y; gbc.gridx = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Image File:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(imageFilePanel, gbc);

        gbc.gridy = ++y; gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 1.0;
        formPanel.add(imagePreviewLabel, gbc);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearButton = new JButton("Clear Form / New");
        JButton addButton = new JButton("Add Product");
        JButton saveButton = new JButton("Save Changes");
        JButton deleteButton = new JButton("Delete Product");

        clearButton.addActionListener(e -> clearForm());
        addButton.addActionListener(e -> handleAddProduct());
        saveButton.addActionListener(e -> handleSaveChanges());
        deleteButton.addActionListener(e -> handleDeleteProduct());

        panel.add(clearButton);
        panel.add(addButton);
        panel.add(saveButton);
        panel.add(deleteButton);
        return panel;
    }

    private void loadCategories() {
        categoryListModel.clear();
        categoryComboBox.removeAllItems();
        List<Category> categories = inventory.getAllCategories();
        categories.forEach(cat -> {
            categoryListModel.addElement(cat);
            categoryComboBox.addItem(cat);
        });
    }

    private void loadProductsForSelectedCategory() {
        productListModel.clear();
        Category selected = categoryList.getSelectedValue();
        if (selected != null) {
            inventory.getProductsByCategory(selected).forEach(productListModel::addElement);
        }
    }

    private void populateForm(Product product) {
        if (product == null) {
            clearForm();
            return;
        }
        nameField.setText(product.getName());
        refField.setText(product.getRefNumber());
        brandField.setText(product.getBrand());
        priceField.setText(String.format("%.2f", product.getPrice()));
        quantitySpinner.setValue(inventory.getStockCount(product));
        imagePathField.setText(product.getImagePath());

        for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
            if (categoryComboBox.getItemAt(i).getId() == product.getCategoryId()) {
                categoryComboBox.setSelectedIndex(i);
                break;
            }
        }

        updateImagePreview(product.getImagePath());
    }

    private void clearForm() {
        productList.clearSelection();
        nameField.setText("");
        refField.setText("");
        brandField.setText("");
        priceField.setText("");
        quantitySpinner.setValue(0);
        categoryComboBox.setSelectedIndex(-1);
        imagePathField.setText("");
        updateImagePreview((String) null);
    }

    private void handleBrowseImage() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "png", "gif", "jpeg");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                String projectRoot = System.getProperty("user.dir");
                // Corrected path to match a simpler project structure (src/resources, not src/main/resources)
                String relativePath = "src" + File.separator + "resources" + File.separator + "images";
                Path destPath = Paths.get(projectRoot, relativePath, selectedFile.getName());

                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                String filename = selectedFile.getName();
                imagePathField.setText(filename);
                updateImagePreview(destPath.toFile()); // Use the File object for immediate preview

                System.out.println("Image copied successfully to: " + destPath + ". Rebuild project for changes to be permanent.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error copying image file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void updateImagePreview(String filename) {
        if (filename != null && !filename.isEmpty()) {
            URL url = getClass().getResource("/images/" + filename);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                imagePreviewLabel.setIcon(new ImageIcon(scaled));
                imagePreviewLabel.setText(null);
                return;
            }
        }
        imagePreviewLabel.setIcon(null);
        imagePreviewLabel.setText("No Image Preview");
    }

    private void updateImagePreview(File imageFile) {
        if (imageFile != null && imageFile.exists()) {
            ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            imagePreviewLabel.setIcon(new ImageIcon(scaled));
            imagePreviewLabel.setText(null);
        } else {
            updateImagePreview((String) null);
        }
    }

    private Product createProductFromFields(int existingId) {
        try {
            String name = nameField.getText();
            String ref = refField.getText();
            String brand = brandField.getText();
            double price = Double.parseDouble(priceField.getText());
            Category cat = (Category) categoryComboBox.getSelectedItem();
            String imgPath = imagePathField.getText();
            int catId = (cat != null) ? cat.getId() : 0;

            if (existingId == -1) {
                return new Product(name, price, ref, brand, imgPath, catId);
            } else {
                return new Product(existingId, name, price, ref, brand, imgPath, catId);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void handleAddProduct() {
        Product newProduct = createProductFromFields(-1);
        if (newProduct != null) {
            int quantity = (int) quantitySpinner.getValue();
            inventory.addProduct(newProduct, quantity);
            loadProductsForSelectedCategory();
            clearForm();
            JOptionPane.showMessageDialog(this, "Product Added Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleSaveChanges() {
        Product selectedProduct = productList.getSelectedValue();
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Product updatedProduct = createProductFromFields(selectedProduct.getId());
        if (updatedProduct != null) {
            int quantity = (int) quantitySpinner.getValue();
            inventory.updateProduct(updatedProduct, quantity);
            loadProductsForSelectedCategory();
            JOptionPane.showMessageDialog(this, "Product Updated Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDeleteProduct() {
        Product selectedProduct = productList.getSelectedValue();
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete '" + selectedProduct.getName() + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            inventory.removeProduct(selectedProduct);
            loadProductsForSelectedCategory();
            clearForm();
        }
    }

    private static class ProductListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Product p) {
                setText(String.format("<html>%s<br><i>(%s)</i></html>", p.getName(), p.getRefNumber()));
            }
            return this;
        }
    }
}
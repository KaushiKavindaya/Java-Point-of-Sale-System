package app;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Data Access Layer for the POS system.
 * This class handles all database interactions related to products and categories.
 * It provides methods for creating, reading, updating, and deleting inventory items.
 */
public class Inventory {

    /**
     * A helper method to map a row from a ResultSet to a Product object.
     * This avoids code duplication in methods that fetch product data.
     *
     * @param rs The ResultSet currently pointing to a product row.
     * @return A new Product object populated with data from the ResultSet.
     * @throws SQLException If a database access error occurs.
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("ref_number"),
                rs.getString("brand"),
                rs.getString("image_path"),
                rs.getInt("category_id")
        );
    }

    /**
     * Retrieves a list of all categories from the database.
     *
     * @return A List of Category objects, sorted by name.
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    /**
     * Retrieves all products belonging to a specific category.
     * Used primarily by the InventoryDialog.
     *
     * @param category The category to filter by. If null, returns all products.
     * @return A List of Product objects.
     */
    public List<Product> getProductsByCategory(Category category) {
        List<Product> products = new ArrayList<>();
        String sql = (category != null)
                ? "SELECT * FROM products WHERE category_id = ? ORDER BY name ASC"
                // A version to get ALL products regardless of category
                : "SELECT * FROM products ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (category != null) {
                pstmt.setInt(1, category.getId());
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Searches for products by name or reference number, with an optional category filter.
     * Used by the main PosFrame.
     *
     * @param searchTerm The text to search for in product names and ref_numbers.
     * @param category   The category to filter by. Can be null to search all categories.
     * @return A List of matching Product objects.
     */
    public List<Product> searchProducts(String searchTerm, Category category) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE (name LIKE ? OR ref_number LIKE ?) AND quantity > 0");
        if (category != null) {
            sql.append(" AND category_id = ?");
        }
        sql.append(" ORDER BY name ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            String likeTerm = "%" + searchTerm + "%";
            pstmt.setString(1, likeTerm);
            pstmt.setString(2, likeTerm);
            if (category != null) {
                pstmt.setInt(3, category.getId());
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Adds a new product to the database.
     *
     * @param product  The Product object containing all details.
     * @param quantity The initial stock quantity.
     */
    public void addProduct(Product product, int quantity) {
        String sql = "INSERT INTO products(name, price, ref_number, brand, image_path, quantity, category_id) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setString(3, product.getRefNumber());
            pstmt.setString(4, product.getBrand());
            pstmt.setString(5, product.getImagePath());
            pstmt.setInt(6, quantity);
            pstmt.setInt(7, product.getCategoryId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing product's details in the database.
     *
     * @param product     The Product object with updated details (must include the correct ID).
     * @param newQuantity The new stock quantity.
     */
    public void updateProduct(Product product, int newQuantity) {
        String sql = "UPDATE products SET name = ?, price = ?, ref_number = ?, brand = ?, image_path = ?, quantity = ?, category_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setString(3, product.getRefNumber());
            pstmt.setString(4, product.getBrand());
            pstmt.setString(5, product.getImagePath());
            pstmt.setInt(6, newQuantity);
            pstmt.setInt(7, product.getCategoryId());
            pstmt.setInt(8, product.getId()); // WHERE clause uses the product's ID
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a product from the database using its ID.
     *
     * @param product The product to be deleted.
     */
    public void removeProduct(Product product) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, product.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the current stock count for a specific product.
     *
     * @param product The product to check.
     * @return The current quantity in stock.
     */
    public int getStockCount(Product product) {
        String sql = "SELECT quantity FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, product.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Reduces the stock count of a product after a sale.
     *
     * @param product         The product that was sold.
     * @param quantityToReduce The number of items sold.
     */
    public void reduceStock(Product product, int quantityToReduce) {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantityToReduce);
            pstmt.setInt(2, product.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a list of all sales transactions.
     * Used by the SalesReportFrame.
     *
     * @return A List of Sale objects, sorted with the most recent first.
     */
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT id, sale_date, total_price, payment_method FROM sales ORDER BY sale_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sales.add(new Sale(
                        rs.getInt("id"),
                        rs.getTimestamp("sale_date"),
                        rs.getBigDecimal("total_price"),
                        rs.getString("payment_method")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    /**
     * Retrieves all line items for a specific sale.
     * Used by the SalesReportFrame.
     *
     * @param saleId The ID of the sale to get items for.
     * @return A List of SaleItem objects.
     */
    public List<SaleItem> getSaleItems(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT p.name, si.quantity, si.price_at_sale " +
                "FROM sale_items si " +
                "LEFT JOIN products p ON si.product_id = p.id " + // LEFT JOIN in case product was deleted
                "WHERE si.sale_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("name");
                    if (productName == null) {
                        productName = "[Deleted Product]"; // Handle case where product was deleted
                    }
                    items.add(new SaleItem(
                            productName,
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price_at_sale")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}
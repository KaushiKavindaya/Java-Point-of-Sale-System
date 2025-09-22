package app;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Map;

/**
 * Handles all database operations related to logging sales transactions.
 * This class uses transactions to ensure data integrity.
 */
public class SalesLogger {

    /**
     * Logs a complete sale, including items and detailed payment information, to the database.
     *
     * @param cart           A map of products and their quantities.
     * @param totalPrice     The total price of the sale.
     * @param paymentMethod  The method of payment ("Cash" or "Card").
     * @param cashTendered   The cash amount given by the customer (for cash sales).
     * @param changeGiven    The change returned to the customer (for cash sales).
     * @param cardType       The type of card used (for card sales).
     */
    public void logSale(Map<Product, Integer> cart, BigDecimal totalPrice, String paymentMethod, BigDecimal cashTendered, BigDecimal changeGiven, String cardType) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into the 'sales' table with the new payment details
            String saleSql = "INSERT INTO sales (total_price, payment_method, cash_tendered, change_given, card_type) VALUES (?, ?, ?, ?, ?)";
            int saleId;

            try (PreparedStatement salePstmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                salePstmt.setBigDecimal(1, totalPrice);
                salePstmt.setString(2, paymentMethod);

                // Handle nullable decimal fields for cash details
                if (cashTendered != null) {
                    salePstmt.setBigDecimal(3, cashTendered);
                } else {
                    salePstmt.setNull(3, Types.DECIMAL);
                }
                if (changeGiven != null) {
                    salePstmt.setBigDecimal(4, changeGiven);
                } else {
                    salePstmt.setNull(4, Types.DECIMAL);
                }

                // Handle nullable string field for card type
                if (cardType != null && !cardType.isEmpty()) {
                    salePstmt.setString(5, cardType);
                } else {
                    salePstmt.setNull(5, Types.VARCHAR);
                }

                salePstmt.executeUpdate();

                // Get the auto-generated ID of the new sale
                try (ResultSet generatedKeys = salePstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        saleId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating sale failed, no ID obtained.");
                    }
                }
            }

            // 2. Insert each product into the 'sale_items' table
            String itemsSql = "INSERT INTO sale_items (sale_id, product_id, quantity, price_at_sale) VALUES (?, ?, ?, ?)";
            try (PreparedStatement itemsPstmt = conn.prepareStatement(itemsSql)) {
                for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                    Product product = entry.getKey();
                    int quantity = entry.getValue();

                    itemsPstmt.setInt(1, saleId);
                    itemsPstmt.setInt(2, product.getId());
                    itemsPstmt.setInt(3, quantity);
                    itemsPstmt.setBigDecimal(4, BigDecimal.valueOf(product.getPrice())); // Use BigDecimal
                    itemsPstmt.addBatch();
                }
                itemsPstmt.executeBatch();
            }

            conn.commit(); // Commit transaction if all operations succeed

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back.");
                    conn.rollback(); // Roll back transaction on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
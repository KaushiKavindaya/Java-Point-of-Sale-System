package app;

import java.util.Objects;

/**
 * Represents a single product in the system. This is a plain old Java object (POJO)
 * that holds data retrieved from or destined for the database.
 */
public class Product {
    private final int id;
    private final String name;
    private final double price;
    private final String refNumber;
    private final String brand;
    private final String imagePath;
    private final int categoryId; // Foreign key to the categories table

    /**
     * Constructor for creating a Product object from data fetched from the database.
     */
    public Product(int id, String name, double price, String refNumber, String brand, String imagePath, int categoryId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.refNumber = refNumber;
        this.brand = brand;
        this.imagePath = imagePath;
        this.categoryId = categoryId;
    }

    /**
     * Constructor for creating a new product that is not yet in the database.
     * The ID is typically a sentinel value like -1.
     */
    public Product(String name, double price, String refNumber, String brand, String imagePath, int categoryId) {
        this.id = -1; // Indicates a new product not yet persisted
        this.name = name;
        this.price = price;
        this.refNumber = refNumber;
        this.brand = brand;
        this.imagePath = imagePath;
        this.categoryId = categoryId;
    }

    // --- Getters for all fields ---
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getRefNumber() { return refNumber; }
    public String getBrand() { return brand; }
    public String getImagePath() { return imagePath; }
    public int getCategoryId() { return categoryId; }

    /**
     * Provides a rich HTML string representation for display in Swing components like JList.
     */
    @Override
    public String toString() {
        return String.format("<html><b>%s</b> (%s)<br>%s - Rs.%.2f</html>", name, refNumber, brand, price);
    }

    /**
     * Two products are considered equal if they have the same database ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    /**
     * The hash code is based on the unique database ID.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
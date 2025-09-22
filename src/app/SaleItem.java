package app;

import java.math.BigDecimal;

public class SaleItem {
    private final String productName;
    private final int quantity;
    private final BigDecimal priceAtSale;

    public SaleItem(String productName, int quantity, BigDecimal priceAtSale) {
        this.productName = productName;
        this.quantity = quantity;
        this.priceAtSale = priceAtSale;
    }

    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPriceAtSale() { return priceAtSale; }

    public BigDecimal getSubtotal() {
        return priceAtSale.multiply(BigDecimal.valueOf(quantity));
    }
}
package app;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Sale {
    private final int id;
    private final Timestamp saleDate;
    private final BigDecimal totalPrice;
    private final String paymentMethod;

    public Sale(int id, Timestamp saleDate, BigDecimal totalPrice, String paymentMethod) {
        this.id = id;
        this.saleDate = saleDate;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
    }

    public int getId() { return id; }
    public Timestamp getSaleDate() { return saleDate; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getPaymentMethod() { return paymentMethod; }

    public String getFormattedDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(saleDate);
    }
}
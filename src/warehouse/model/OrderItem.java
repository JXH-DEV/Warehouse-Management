package warehouse.model;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int orderId;
    private Product product;
    private int quantity;
    private double unitPrice;

    public OrderItem(int id, int orderId, Product product, int quantity, double unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getSubtotal() { return quantity * unitPrice; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}

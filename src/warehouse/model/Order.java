package warehouse.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED }
    public enum Type { INCOMING, OUTGOING }

    private int id;
    private String orderNumber;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private Status status;
    private Type type;
    private String supplierOrClient;
    private List<OrderItem> items;
    private String notes;
    private int createdByUserId;
    private boolean stockApplied;

    public Order(int id, String orderNumber, LocalDate orderDate, LocalDate deliveryDate,
                 Status status, Type type, String supplierOrClient, String notes, int createdByUserId) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.type = type;
        this.supplierOrClient = supplierOrClient;
        this.notes = notes;
        this.createdByUserId = createdByUserId;
        this.items = new ArrayList<>();
        this.stockApplied = false;
    }

    public int getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public LocalDate getOrderDate() { return orderDate; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public Status getStatus() { return status; }
    public Type getType() { return type; }
    public String getSupplierOrClient() { return supplierOrClient; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public String getNotes() { return notes; }
    public int getCreatedByUserId() { return createdByUserId; }
    public boolean isStockApplied() { return stockApplied; }

    public void setStatus(Status status) { this.status = status; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStockApplied(boolean stockApplied) { this.stockApplied = stockApplied; }
    public void addItem(OrderItem item) { items.add(item); }
    public boolean removeItem(int itemId) { return items.removeIf(i -> i.getId() == itemId); }

    public double getTotalAmount() {
        return items.stream().mapToDouble(OrderItem::getSubtotal).sum();
    }

    @Override
    public String toString() { return orderNumber; }
}

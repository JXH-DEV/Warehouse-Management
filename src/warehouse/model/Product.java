package warehouse.model;

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Category { ELEKTRONIKE, USHQIM, VESHJE, ELEKTROSHTEPIAK, TJETER }

    private int id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private Category category;
    private String unit;
    private int minStock;

    public Product(int id, String name, String description, double price,
                   int quantity, Category category, String unit, int minStock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.unit = unit;
        this.minStock = minStock;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public Category getCategory() { return category; }
    public String getUnit() { return unit; }
    public int getMinStock() { return minStock; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setCategory(Category category) { this.category = category; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setMinStock(int minStock) { this.minStock = minStock; }

    public boolean isLowStock() { return quantity <= minStock; }
    public double getTotalValue() { return price * quantity; }

    @Override
    public String toString() { return name; }
}

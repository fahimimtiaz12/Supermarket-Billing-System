package com.example.supermarketbillingsystem;

import javafx.beans.property.*;

public class Product {
    private StringProperty name;
    private StringProperty category;
    private IntegerProperty quantity;
    private DoubleProperty price;
    private String productCode;

    public Product(String name, String category, String productCode, int quantity, double price) {
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.productCode = productCode;
        this.quantity = new SimpleIntegerProperty(quantity);  // This is the editable quantity
        this.price = new SimpleDoubleProperty(price);
    }

    public String getProductCode() {
        return productCode;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getCategory() {
        return category.get();
    }

    public Integer getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);  // Allow updating the quantity
    }

    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public double getTotalPrice() {
        return price.get() * quantity.get();  // Total price = quantity * price
    }
}

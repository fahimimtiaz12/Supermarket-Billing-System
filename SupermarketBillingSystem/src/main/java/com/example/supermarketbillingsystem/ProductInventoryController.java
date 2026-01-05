package com.example.supermarketbillingsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class ProductInventoryController {

    @FXML
    private TableView<Product> cartTable;

    @FXML
    private TableColumn<Product, String> itemCol;

    @FXML
    private TableColumn<Product, Integer> qtyCol;

    @FXML
    private TableColumn<Product, Double> priceCol;

    @FXML
    private TextField nameField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField searchField;

    private ObservableList<Product> cart = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Bind the table columns to the Product properties
        itemCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        qtyCol.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            int newQuantity = event.getNewValue();

            // Fetch the available stock from the database
            int availableStock = product.getQuantity();

            // Check if the new quantity is less than or equal to available stock
            if (newQuantity <= availableStock) {
                product.setQuantity(newQuantity);
                updateTotal();
            } else {
                showError("Not enough stock available. Only " + availableStock + " units available.");
            }
        });

        cartTable.setItems(cart);
    }

    // Add product to the cart with a default quantity of 1
    public void addProductToCart(String productCode, int quantityToAdd) {
        Product product = getProductFromDatabase(productCode);

        if (product != null) {
            int availableStock = product.getQuantity();

            // If no quantity is specified, set the default quantity to 1
            if (quantityToAdd == 0) {
                quantityToAdd = 1; // Default quantity of 1
            }

            // Check if the quantity is within available stock
            if (quantityToAdd <= availableStock) {
                product.setQuantity(quantityToAdd);  // Set the quantity in the product
                cart.add(product);  // Add product to the cart
                updateTotal();  // Update the total
                updateProductStockInDatabase(product, quantityToAdd);  // Update stock in database
            } else {
                showError("Not enough stock available. Only " + availableStock + " units available.");
            }
        }
    }

    // Fetch product from the database
    private Product getProductFromDatabase(String productCode) {
        return DatabaseUtil.getProductByCode(productCode);
    }

    // Update the total price label
    private void updateTotal() {
        double total = cart.stream().mapToDouble(Product::getTotalPrice).sum();
        DecimalFormat df = new DecimalFormat("0.00");
        // You can update the label here for total (e.g. totalLabel.setText(df.format(total)))
    }

    @FXML
    private void searchProductByCode(ActionEvent event) {
        String searchCode = searchField.getText().trim();

        if (!searchCode.isEmpty()) {
            Product product = getProductFromDatabase(searchCode);
            if (product != null) {
                // Add product with a default quantity of 1
                addProductToCart(searchCode, 0);  // 0 here indicates that the default quantity should be 1
            } else {
                showError("Product not found.");
            }
        }
    }

    // Show an error message
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    // Method to update product stock in the database
    private void updateProductStockInDatabase(Product product, int quantityToAdd) {
        try (Connection conn = DatabaseUtil.connect()) {
            String query = "UPDATE products SET quantity = quantity - ? WHERE product_code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, quantityToAdd);  // Subtract the added quantity
                stmt.setString(2, product.getProductCode());
                stmt.executeUpdate();  // Update stock in the database
            }
        } catch (SQLException e) {
            showError("Error while updating product stock in database.");
            e.printStackTrace();
        }
    }
}

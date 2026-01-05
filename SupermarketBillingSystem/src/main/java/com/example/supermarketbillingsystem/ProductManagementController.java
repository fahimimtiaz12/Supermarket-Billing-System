package com.example.supermarketbillingsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;

public class ProductManagementController {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, String> productCodeColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableColumn<Product, Integer> quantityColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;

    @FXML
    private TableColumn<Product, Double> totalPriceColumn;

    @FXML
    private TextField productCodeField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField categoryField;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField priceField;

    @FXML
    private Button addButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button clearButton;

    private ObservableList<Product> productList;

    public ProductManagementController() {
        // Constructor
    }

    @FXML
    private void initialize() {
        // Set up table columns
        productCodeColumn.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Fix for total price column
        totalPriceColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            return new javafx.beans.property.SimpleDoubleProperty(product.getTotalPrice()).asObject();
        });

        // Load data
        loadProducts();

        // Table selection listener
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateFields(newValue);
                    }
                }
        );
    }

    private void loadProducts() {
        productList = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseUtil.connect();
            String query = "SELECT product_code, product_name, category, quantity, price FROM products";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Product product = new Product(
                        resultSet.getString("product_name"),
                        resultSet.getString("category"),
                        resultSet.getString("product_code"),
                        resultSet.getInt("quantity"),
                        resultSet.getDouble("price")
                );
                productList.add(product);
            }

            productTable.setItems(productList);
            conn.close();

        } catch (SQLException e) {
            showAlert("Error", "Failed to load products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateFields(Product product) {
        productCodeField.setText(product.getProductCode());
        nameField.setText(product.getName());
        categoryField.setText(product.getCategory());
        quantityField.setText(String.valueOf(product.getQuantity()));
        priceField.setText(String.valueOf(product.getPrice()));
    }

    @FXML
    private void handleAddProduct() {
        try {
            String productCode = productCodeField.getText().trim();
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());

            if (productCode.isEmpty() || name.isEmpty() || category.isEmpty()) {
                showAlert("Validation Error", "Please fill all required fields");
                return;
            }

            Connection conn = DatabaseUtil.connect();
            String query = "INSERT INTO products (product_code, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, productCode);
            statement.setString(2, name);
            statement.setString(3, category);
            statement.setInt(4, quantity);
            statement.setDouble(5, price);

            statement.executeUpdate();

            showAlert("Success", "Product added successfully!");
            clearFields();
            loadProducts();
            conn.close();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for quantity and price");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateProduct() {
        try {
            String productCode = productCodeField.getText().trim();
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());

            if (productCode.isEmpty() || name.isEmpty() || category.isEmpty()) {
                showAlert("Validation Error", "Please fill all required fields");
                return;
            }

            Connection conn = DatabaseUtil.connect();
            String query = "UPDATE products SET product_name=?, category=?, quantity=?, price=? WHERE product_code=?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setInt(3, quantity);
            statement.setDouble(4, price);
            statement.setString(5, productCode);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Success", "Product updated successfully!");
                clearFields();
                loadProducts();
            } else {
                showAlert("Error", "No product found with the given code");
            }
            conn.close();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for quantity and price");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteProduct() {
        try {
            String productCode = productCodeField.getText().trim();

            if (productCode.isEmpty()) {
                showAlert("Validation Error", "Please select a product to delete");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Are you sure you want to delete this product?");
            alert.setContentText("This action cannot be undone.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                Connection conn = DatabaseUtil.connect();
                String query = "DELETE FROM products WHERE product_code=?";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, productCode);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert("Success", "Product deleted successfully!");
                    clearFields();
                    loadProducts();
                } else {
                    showAlert("Error", "No product found with the given code");
                }
                conn.close();
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to delete product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

    private void clearFields() {
        productCodeField.clear();
        nameField.clear();
        categoryField.clear();
        quantityField.clear();
        priceField.clear();
        productTable.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) productTable.getScene().getWindow();
        stage.close();
    }
}
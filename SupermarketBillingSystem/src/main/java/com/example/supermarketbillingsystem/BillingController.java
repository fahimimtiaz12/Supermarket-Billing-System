package com.example.supermarketbillingsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class BillingController {
    private String currentUsername;
    private String currentUserRole;

    public void setUserInfo(String username, String role) {
        this.currentUsername = username;
        this.currentUserRole = role;
    }

    @FXML
    private TableView<Product> cartTable;

    @FXML
    private TableColumn<Product, String> itemCol;

    @FXML
    private TableColumn<Product, Integer> qtyCol;

    @FXML
    private TableColumn<Product, Double> priceCol;

    @FXML
    private Label totalLabel;

    @FXML
    private TextField cashField;

    @FXML
    private Label balanceLabel;

    @FXML
    private TextField discountField;

    @FXML
    private TextField searchField;

    private ObservableList<Product> cart = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Bind the table columns to the Product properties
        itemCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        // Set the quantity column to be editable
        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        // Listen for changes in the quantity and update the product's quantity
        qtyCol.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            int newQuantity = event.getNewValue();  // New quantity entered by the user

            // Re-fetch the stock from the database to make sure it's up to date
            int availableStock = getProductStockFromDatabase(product.getProductCode());

            // Check if the new quantity is within available stock
            if (newQuantity <= availableStock) {
                product.setQuantity(newQuantity);  // Update the product's quantity in the cart
                updateTotal();  // Recalculate the total
                updateProductStockInDatabase(product, newQuantity); // Update the stock in the database
            } else {
                showError("Not enough stock available. Only " + availableStock + " units available.");
            }
        });

        // Set the TableView items to the cart
        cartTable.setItems(cart);
        cartTable.setEditable(true);  // Ensure the TableView is editable

        // Add listener to discount field to update total when discount changes
        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateTotal();
        });
    }

    @FXML
    private void searchProductByCode(ActionEvent event) {
        String searchCode = searchField.getText().trim();

        if (!searchCode.isEmpty()) {
            Product product = getProductFromDatabase(searchCode);
            if (product != null) {
                int quantityRequested = 1;  // Default quantity to 1
                addProductToCart(searchCode, quantityRequested);
            } else {
                showError("Product not found.");
            }
        }
    }


    @FXML
    private void proceedToPayment(ActionEvent event) {
        if (cart == null || cart.isEmpty()) {
            showError("Cart is empty. Please add items to the cart.");
            return;
        }

        try {
            // Load Payment screen (payment.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/supermarketbillingsystem/payment.fxml"));
            Parent paymentView = loader.load();

            // Pass the cart to PaymentController
            PaymentController paymentController = loader.getController();
            paymentController.setCart(cart);  // Pass the cart

            // Switch to the Payment screen
            Scene paymentScene = new Scene(paymentView);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(paymentScene);
            window.setTitle("Payment");
            window.show();
        } catch (IOException e) {
            System.err.println("Error loading the Payment screen.");
            e.printStackTrace();
        }
    }




    // Add product to the cart with a default quantity of 1
    public void addProductToCart(String productCode, int quantityToAdd) {
        Product product = getProductFromDatabase(productCode);

        if (product != null) {
            int availableStock = product.getQuantity();

            // If no quantity is specified, set the default quantity to 1
            if (quantityToAdd == 0) {
                quantityToAdd = 1;
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

    // Re-fetch the available stock from the database
    private int getProductStockFromDatabase(String productCode) {
        Product product = DatabaseUtil.getProductByCode(productCode);
        return product != null ? product.getQuantity() : 0;
    }

    // Update the total price label with discount applied
    private void updateTotal() {
        double total = cart.stream().mapToDouble(Product::getTotalPrice).sum();

        // Apply discount if specified
        String discountText = discountField.getText().trim();
        if (!discountText.isEmpty()) {
            try {
                double discount = Double.parseDouble(discountText);
                if (discount >= 0 && discount <= 100) {
                    total = total - (total * discount / 100);
                }
            } catch (NumberFormatException e) {
                // Ignore invalid discount values
            }
        }

        DecimalFormat df = new DecimalFormat("0.00");
        totalLabel.setText("$" + df.format(total));
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

    @FXML
    private void goBackToDashboard(ActionEvent event) {
        // Get the current stage
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Get the existing dashboard scene from stage's user data (if saved earlier)
        Scene dashboardScene = (Scene) window.getUserData();

        if (dashboardScene != null) {
            // Simply set the scene without reloading FXML
            window.setScene(dashboardScene);

            // Get the controller and refresh UI
            DashboardController dashboardController = (DashboardController) dashboardScene.getUserData();
            dashboardController.updateUIBasedOnRole();
        } else {
            // First-time load fallback
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/supermarketbillingsystem/dashboard.fxml"));
                Parent dashboardView = loader.load();

                DashboardController dashboardController = loader.getController();
                dashboardController.setCashierInfo(currentUsername, "online");
                dashboardController.setUserRole(currentUserRole);

                Scene scene = new Scene(dashboardView);
                // Save scene for later
                window.setUserData(scene);
                scene.setUserData(dashboardController);

                window.setScene(scene);
                window.setTitle("Dashboard");
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Handle payment action
    @FXML
    private void handlePayment(ActionEvent event) {
        try {
            double cash = Double.parseDouble(cashField.getText());

            // Get the total from the label (which already includes discount)
            String totalText = totalLabel.getText().replace("$", "");
            double total = Double.parseDouble(totalText);

            double balance = cash - total;

            DecimalFormat df = new DecimalFormat("0.00");
            balanceLabel.setText("$" + df.format(balance));
        } catch (NumberFormatException e) {
            showError("Invalid input for cash.");
        }
    }
}
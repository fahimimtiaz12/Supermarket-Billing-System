package com.example.supermarketbillingsystem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardController {

    @FXML
    private Label cashierNameLabel;
    @FXML
    private Label cashierStatusLabel;

    // Add these fields to track user role
    private String currentUserRole;
    private String currentUsername;

    // Button references for role-based hiding
    @FXML
    private Button totalIncomeButton;
    @FXML
    private Button productManagementButton;
    @FXML
    private Button billingButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button roleManagementButton; // Added for role management button

    // Method to set the cashier's information (name and status)
    public void setCashierInfo(String username, String status) {
        cashierNameLabel.setText(username);
        cashierStatusLabel.setText(status);
        // Style the status label based on status
        if ("online".equalsIgnoreCase(status)) {
            cashierStatusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else {
            cashierStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }

        // Store username for later use
        this.currentUsername = username;
    }

    // Method to set user role
    public void setUserRole(String role) {
        this.currentUserRole = role;
        // Update UI based on role
        updateUIBasedOnRole();
    }

    // Method to update UI elements based on user role
    public void updateUIBasedOnRole() {
        if (currentUserRole == null) return;

        // Only Information Manager can see Role Management
        if (roleManagementButton != null) {
            roleManagementButton.setVisible(currentUserRole.equalsIgnoreCase("information_manager"));
        }

        // Other buttons
        if (totalIncomeButton != null) {
            totalIncomeButton.setVisible(currentUserRole.equalsIgnoreCase("manager")
                    || currentUserRole.equalsIgnoreCase("admin"));
        }

        if (productManagementButton != null) {
            productManagementButton.setVisible(currentUserRole.equalsIgnoreCase("manager")
                    || currentUserRole.equalsIgnoreCase("admin")
                    || currentUserRole.equalsIgnoreCase("information_manager"));
        }

        if (billingButton != null) {
            billingButton.setVisible(currentUserRole.equalsIgnoreCase("cashier")
                    || currentUserRole.equalsIgnoreCase("admin"));
        }
    }




    @FXML
    private void openBilling(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/supermarketbillingsystem/billing.fxml"));
            Parent billingView = loader.load();

            BillingController billingController = loader.getController();
            billingController.setUserInfo(currentUsername, currentUserRole);

            Scene billingScene = new Scene(billingView);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Store current dashboard scene for back navigation
            window.setUserData(((Node) event.getSource()).getScene());

            window.setScene(billingScene);
            window.setTitle("Billing");
            window.show();

        } catch (IOException e) {
            showAlert("Error", "Error loading the Billing screen: " + e.getMessage());
        }
    }



    // Method to handle logout action
    @FXML
    public void logout(ActionEvent event) {
        updateCashierStatusOffline(currentUsername);
        loadLoginScreen();
    }

    // Update the cashier's status to 'offline' when logged out
    private void updateCashierStatusOffline(String username) {
        String query = "UPDATE users SET status = 'offline' WHERE username = ?";
        try (Connection conn = DatabaseUtil.connect()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Database Error", "Error while updating cashier status: " + e.getMessage());
        }
    }

    @FXML
    private void openTotalIncomeView(ActionEvent event) {
        // Check user role before allowing access
        if (!"admin".equalsIgnoreCase(currentUserRole) && !"manager".equalsIgnoreCase(currentUserRole)) {
            showAlert("Access Denied", "You do not have permission to view Total Income.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/supermarketbillingsystem/total_income.fxml"));
            Parent totalIncomeView = loader.load();

            // Pass the current role and username to TotalIncomeController
            TotalIncomeController controller = loader.getController();
            controller.setRoleAndUsername(currentUserRole, currentUsername);

            Scene totalIncomeScene = new Scene(totalIncomeView);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(totalIncomeScene);
            window.setTitle("Total Income");
            window.show();
        } catch (IOException e) {
            showAlert("Error", "Error loading the Total Income screen: " + e.getMessage());
        }
    }


    // Add this method to your existing DashboardController.java
    @FXML
    private void openRoleManagement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/supermarketbillingsystem/role_management.fxml"));
            Parent roleManagementView = loader.load();

            // Get the controller
            RoleManagementController roleController = loader.getController();

            // Pass username + role from dashboard
            roleController.setUserInfo(cashierNameLabel.getText(), currentUserRole);

            Scene roleManagementScene = new Scene(roleManagementView);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(roleManagementScene);
            window.setTitle("Role Management");
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading role management screen: " + e.getMessage());
        }
    }


    // Enhanced product management with beautiful UI
    @FXML
    private void openProductManagement(ActionEvent event) {
        // Check user role before allowing access
        if (!"admin".equalsIgnoreCase(currentUserRole) && !"manager".equalsIgnoreCase(currentUserRole) && !"information_manager".equalsIgnoreCase(currentUserRole)) {
            showAlert("Access Denied", "You do not have permission to manage products.");
            return;
        }

        try {
            Stage stage = new Stage();
            stage.setTitle("Product Management");
            stage.initStyle(StageStyle.DECORATED);

            // Create main layout with background
            VBox mainLayout = new VBox(20);
            mainLayout.setPadding(new Insets(25));
            mainLayout.setAlignment(Pos.TOP_CENTER);
            mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #c3cfe2);");

            // Header with title
            Label titleLabel = new Label("Product Management");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            titleLabel.setTextFill(Color.web("#2c3e50"));

            // Info text
            Label infoLabel = new Label("Manage products below:");
            infoLabel.setFont(Font.font("System", 14));
            infoLabel.setTextFill(Color.web("#34495e"));

            // Create a scrollable table for products
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 10;");

            VBox productsContainer = new VBox(10);
            productsContainer.setPadding(new Insets(10));
            productsContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

            // Load products from database
            loadProducts(productsContainer);

            scrollPane.setContent(productsContainer);
            scrollPane.setPrefViewportHeight(300);

            // Button container with styled buttons
            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);

            Button addButton = createStyledButton("Add Product", "#2ecc71");
            Button editButton = createStyledButton("Edit Product", "#3498db");
            Button deleteButton = createStyledButton("Delete Product", "#e74c3c");
            Button closeButton = createStyledButton("Close", "#95a5a6");

            // Button actions
            addButton.setOnAction(e -> showAddProductForm(stage, productsContainer));
            editButton.setOnAction(e -> showEditProductForm(stage, productsContainer));
            deleteButton.setOnAction(e -> showDeleteProductForm(stage, productsContainer));
            closeButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(addButton, editButton, deleteButton, closeButton);

            // Add components to layout
            mainLayout.getChildren().addAll(titleLabel, infoLabel, scrollPane, buttonBox);

            // Set up scene and stage
            Scene scene = new Scene(mainLayout, 600, 550);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Could not open product management: " + e.getMessage());
        }
    }

    // Helper method to create styled buttons
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 10 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", 20%); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 10 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3);"));

        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 10 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        return button;
    }

    // Navigate back to login screen
    private void loadLoginScreen() {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene loginScene = new Scene(loginView);
            Stage window = (Stage) cashierNameLabel.getScene().getWindow();
            window.setScene(loginScene);
            window.setTitle("Login");
            window.show();
        } catch (IOException e) {
            showAlert("Error", "Error loading login screen: " + e.getMessage());
        }
    }

    // Helper method for showing alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f5f5f5;");
        dialogPane.lookup(".content.label").setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        alert.showAndWait();
    }

    // Load products from database and display them with beautiful cards
    private void loadProducts(VBox productsContainer) {
        productsContainer.getChildren().clear();

        // Add header
        HBox headerBox = new HBox();
        headerBox.setPadding(new Insets(5, 10, 10, 10));
        headerBox.setSpacing(20);
        headerBox.setStyle("-fx-background-color: #3498db; -fx-background-radius: 5;");

        Label nameHeader = new Label("Product Name");
        nameHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-min-width: 150;");

        Label priceHeader = new Label("Price");
        priceHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-min-width: 80;");

        Label quantityHeader = new Label("Qty");
        quantityHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-min-width: 50;");

        Label codeHeader = new Label("Code");
        codeHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-min-width: 80;");

        headerBox.getChildren().addAll(nameHeader, priceHeader, quantityHeader, codeHeader);
        productsContainer.getChildren().add(headerBox);

        try (Connection conn = DatabaseUtil.connect()) {
            String query = "SELECT id, name, price, quantity, product_code FROM products";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                int quantity = resultSet.getInt("quantity");
                String productCode = resultSet.getString("product_code");

                // Create a product card
                HBox productCard = new HBox();
                productCard.setPadding(new Insets(10));
                productCard.setSpacing(20);
                productCard.setAlignment(Pos.CENTER_LEFT);
                productCard.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #e9ecef; -fx-border-radius: 5;");

                // Add hover effect
                productCard.setOnMouseEntered(e -> productCard.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 5; -fx-border-color: #bbdefb; -fx-border-radius: 5;"));
                productCard.setOnMouseExited(e -> productCard.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #e9ecef; -fx-border-radius: 5;"));

                Label nameLabel = new Label(name);
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-min-width: 150;");

                Label priceLabel = new Label("$" + String.format("%.2f", price));
                priceLabel.setStyle("-fx-text-fill: #27ae60; -fx-min-width: 80;");

                Label quantityLabel = new Label(String.valueOf(quantity));
                // Color code based on quantity
                if (quantity < 10) {
                    quantityLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-min-width: 50;");
                } else {
                    quantityLabel.setStyle("-fx-text-fill: #2c3e50; -fx-min-width: 50;");
                }

                Label codeLabel = new Label(productCode);
                codeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-min-width: 80;");

                productCard.getChildren().addAll(nameLabel, priceLabel, quantityLabel, codeLabel);
                productsContainer.getChildren().add(productCard);
            }

        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading products: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            productsContainer.getChildren().add(errorLabel);
        }
    }

    // Show form to add a new product with all fields
    private void showAddProductForm(Stage parentStage, VBox productsContainer) {
        try {
            Stage formStage = new Stage();
            formStage.setTitle("Add New Product");
            formStage.initStyle(StageStyle.UTILITY);
            formStage.initOwner(parentStage);

            VBox formLayout = new VBox(15);
            formLayout.setPadding(new Insets(25));
            formLayout.setStyle("-fx-background-color: #ecf0f1;");

            Label formTitle = new Label("Add New Product");
            formTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
            formTitle.setTextFill(Color.web("#2c3e50"));

            // Create form fields with prompts
            TextField nameField = createStyledTextField("Product Name");
            TextField categoryField = createStyledTextField("Category");
            TextField codeField = createStyledTextField("Product Code");
            TextField priceField = createStyledTextField("Price");
            TextField quantityField = createStyledTextField("Quantity");

            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);

            Button saveButton = createStyledButton("Save", "#2ecc71");
            Button cancelButton = createStyledButton("Cancel", "#95a5a6");

            // Button actions
            saveButton.setOnAction(e -> {
                try {
                    String name = nameField.getText().trim();
                    String category = categoryField.getText().trim();
                    String code = codeField.getText().trim();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    double price = Double.parseDouble(priceField.getText().trim());

                    if (name.isEmpty() || category.isEmpty() || code.isEmpty()) {
                        showAlert("Validation Error", "Please fill all required fields");
                        return;
                    }

                    // Insert into database
                    String insertQuery = "INSERT INTO products (name, category, price, quantity, product_code) VALUES (?, ?, ?, ?, ?)";
                    try (Connection conn = DatabaseUtil.connect()) {
                        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, category);
                        insertStmt.setDouble(3, price);
                        insertStmt.setInt(4, quantity);
                        insertStmt.setString(5, code);
                        insertStmt.executeUpdate();
                    }

                    showAlert("Success", "Product added successfully!");
                    loadProducts(productsContainer); // Refresh the product list
                    formStage.close();

                } catch (NumberFormatException e1) {
                    showAlert("Input Error", "Please enter valid numbers for price and quantity");
                } catch (SQLException e1) {
                    showAlert("Database Error", "Failed to add product: " + e1.getMessage());
                }
            });

            cancelButton.setOnAction(e -> formStage.close());

            buttonBox.getChildren().addAll(saveButton, cancelButton);

            // Add components to form
            formLayout.getChildren().addAll(formTitle, nameField, categoryField, codeField,
                    priceField, quantityField, buttonBox);

            // Create and show the dialog
            Scene formScene = new Scene(formLayout, 400, 450);
            formStage.setScene(formScene);
            formStage.show();

        } catch (Exception e) {
            showAlert("Error", "Could not open add product form: " + e.getMessage());
        }
    }

    // Helper method to create styled text fields
    private TextField createStyledTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; " +
                "-fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        return textField;
    }

    // Show form to edit an existing product
    private void showEditProductForm(Stage parentStage, VBox productsContainer) {
        try {
            Stage formStage = new Stage();
            formStage.setTitle("Edit Product");
            formStage.initStyle(StageStyle.UTILITY);
            formStage.initOwner(parentStage);

            VBox formLayout = new VBox(15);
            formLayout.setPadding(new Insets(25));
            formLayout.setStyle("-fx-background-color: #ecf0f1;");

            Label formTitle = new Label("Edit Product");
            formTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
            formTitle.setTextFill(Color.web("#2c3e50"));

            // Create form fields with prompts
            TextField codeField = createStyledTextField("Product Code (required)");
            TextField nameField = createStyledTextField("Product Name");
            TextField categoryField = createStyledTextField("Category");
            TextField priceField = createStyledTextField("Price");
            TextField quantityField = createStyledTextField("Quantity");

            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);

            Button saveButton = createStyledButton("Save Changes", "#3498db");
            Button cancelButton = createStyledButton("Cancel", "#95a5a6");

            // Button actions
            saveButton.setOnAction(e -> {
                try {
                    String code = codeField.getText().trim();
                    String name = nameField.getText().trim();
                    String category = categoryField.getText().trim();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    double price = Double.parseDouble(priceField.getText().trim());

                    if (code.isEmpty() || name.isEmpty() || category.isEmpty()) {
                        showAlert("Validation Error", "Please fill all required fields");
                        return;
                    }

                    // Update in database
                    String updateQuery = "UPDATE products SET name=?, category=?, price=?, quantity=? WHERE product_code=?";
                    try (Connection conn = DatabaseUtil.connect()) {
                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setString(1, name);
                        updateStmt.setString(2, category);
                        updateStmt.setDouble(3, price);
                        updateStmt.setInt(4, quantity);
                        updateStmt.setString(5, code);
                        int rowsAffected = updateStmt.executeUpdate();

                        if (rowsAffected > 0) {
                            showAlert("Success", "Product updated successfully!");
                            loadProducts(productsContainer);
                        } else {
                            showAlert("Error", "No product found with the given code");
                        }
                    }

                    formStage.close();

                } catch (NumberFormatException e1) {
                    showAlert("Input Error", "Please enter valid numbers for price and quantity");
                } catch (SQLException e1) {
                    showAlert("Database Error", "Failed to update product: " + e1.getMessage());
                }
            });

            cancelButton.setOnAction(e -> formStage.close());

            buttonBox.getChildren().addAll(saveButton, cancelButton);

            // Add components to form
            formLayout.getChildren().addAll(formTitle, codeField, nameField, categoryField,
                    priceField, quantityField, buttonBox);

            // Create and show the dialog
            Scene formScene = new Scene(formLayout, 400, 450);
            formStage.setScene(formScene);
            formStage.show();

        } catch (Exception e) {
            showAlert("Error", "Could not open edit product form: " + e.getMessage());
        }
    }

    // Show form to delete an existing product
    private void showDeleteProductForm(Stage parentStage, VBox productsContainer) {
        try {
            Stage formStage = new Stage();
            formStage.setTitle("Delete Product");
            formStage.initStyle(StageStyle.UTILITY);
            formStage.initOwner(parentStage);

            VBox formLayout = new VBox(15);
            formLayout.setPadding(new Insets(25));
            formLayout.setStyle("-fx-background-color: #ecf0f1;");

            Label formTitle = new Label("Delete Product");
            formTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
            formTitle.setTextFill(Color.web("#2c3e50"));

            Label warningLabel = new Label("Warning: This action cannot be undone!");
            warningLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            TextField codeField = createStyledTextField("Enter Product Code to delete");

            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);

            Button deleteButton = createStyledButton("Delete", "#e74c3c");
            Button cancelButton = createStyledButton("Cancel", "#95a5a6");

            // Button actions
            deleteButton.setOnAction(e -> {
                try {
                    String code = codeField.getText().trim();

                    if (code.isEmpty()) {
                        showAlert("Validation Error", "Please enter a product code");
                        return;
                    }

                    // Confirm deletion
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Confirm Delete");
                    confirmAlert.setHeaderText("Are you sure you want to delete this product?");
                    confirmAlert.setContentText("This action cannot be undone.");

                    // Style the confirmation dialog
                    DialogPane dialogPane = confirmAlert.getDialogPane();
                    dialogPane.setStyle("-fx-background-color: #f5f5f5;");
                    dialogPane.lookup(".content.label").setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");

                    if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                        // Delete from database
                        String deleteQuery = "DELETE FROM products WHERE product_code=?";
                        try (Connection conn = DatabaseUtil.connect()) {
                            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                            deleteStmt.setString(1, code);
                            int rowsAffected = deleteStmt.executeUpdate();

                            if (rowsAffected > 0) {
                                showAlert("Success", "Product deleted successfully!");
                                loadProducts(productsContainer);
                            } else {
                                showAlert("Error", "No product found with the given code");
                            }
                        }
                    }

                    formStage.close();

                } catch (SQLException e1) {
                    showAlert("Database Error", "Failed to delete product: " + e1.getMessage());
                }
            });

            cancelButton.setOnAction(e -> formStage.close());

            buttonBox.getChildren().addAll(deleteButton, cancelButton);

            // Add components to form
            formLayout.getChildren().addAll(formTitle, warningLabel, codeField, buttonBox);

            // Create and show the dialog
            Scene formScene = new Scene(formLayout, 400, 250);
            formStage.setScene(formScene);
            formStage.show();

        } catch (Exception e) {
            showAlert("Error", "Could not open delete product form: " + e.getMessage());
        }
    }
}

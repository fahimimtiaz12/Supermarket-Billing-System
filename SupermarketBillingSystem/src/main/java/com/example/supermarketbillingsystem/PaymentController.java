package com.example.supermarketbillingsystem;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentController {

    @FXML
    private ComboBox<String> paymentMethodComboBox;  // Payment method dropdown (Cash, Card, WeChat, Alipay)

    @FXML
    private TextField cashField;  // Cash input field

    private ObservableList<Product> cart; // Cart containing products
    private double discount = 0; // To hold the discount value

    // Method to set the cart
    public void setCart(ObservableList<Product> cart) {
        this.cart = cart;  // Ensure cart is set correctly
    }

    // Method to set the discount value
    public void setDiscountValue(double discount) {
        this.discount = discount;
    }

    // Handle the submit payment action
    @FXML
    private void submitPayment(ActionEvent event) {
        String selectedPaymentMethod = paymentMethodComboBox.getValue();

        if (cart == null || cart.isEmpty()) {
            showError("No items in the cart. Please add items.");
            return;
        }

        if (selectedPaymentMethod == null) {
            showError("Please select a payment method.");
            return;
        }

        double totalAmount = cart.stream().mapToDouble(Product::getTotalPrice).sum();
        double paymentAmount = 0;

        // For Cash payment
        if (selectedPaymentMethod.equals("Cash")) {
            try {
                paymentAmount = Double.parseDouble(cashField.getText());
            } catch (NumberFormatException e) {
                showError("Invalid cash amount. Please enter a valid number.");
                return;
            }
        }
        // For Card, WeChat, Alipay (assuming full payment)
        else {
            paymentAmount = totalAmount;
        }

        // Apply discount to the total amount
        double discountedTotal = totalAmount - (totalAmount * discount / 100);

        // Check if payment is sufficient
        if (paymentAmount >= discountedTotal) {
            double change = paymentAmount - discountedTotal;
            showSuccess("Payment successful! Your change: $" + change);

            // Update total income in the database
            updateTotalIncome(discountedTotal);

            // Generate the receipt and open the receipt screen
            String receiptText = generateReceipt(totalAmount, paymentAmount, selectedPaymentMethod, discountedTotal);
            openReceiptScreen(receiptText);
        } else {
            showError("Insufficient payment. Please enter a valid amount.");
        }
    }

    // Example method to generate receipt
    private String generateReceipt(double totalAmount, double paymentAmount, String paymentMethod, double discountedTotal) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Supermarket Receipt\n----------------------\n");

        // Add cart items to the receipt
        for (Product product : cart) {
            receipt.append(product.getName()).append(" - $").append(product.getTotalPrice()).append("\n");
        }

        receipt.append("----------------------\n");

        // Apply discount and show the discounted total
        if (discount > 0) {
            receipt.append("Discount: ").append(discount).append("%\n");
            receipt.append("Discounted Total: $").append(discountedTotal).append("\n");
        }

        receipt.append("Total: $").append(totalAmount).append("\n");
        receipt.append("Payment Method: ").append(paymentMethod).append("\n");

        if (paymentMethod.equals("Cash")) {
            double change = paymentAmount - discountedTotal;
            receipt.append("Cash Paid: $").append(paymentAmount).append("\n");
            receipt.append("Change: $").append(change).append("\n");
        }

        receipt.append("Thank you for shopping with us!");
        return receipt.toString();
    }

    // Example method to navigate to the receipt screen
    private void openReceiptScreen(String receiptText) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/supermarketbillingsystem/receipt.fxml"));
            Parent receiptView = loader.load();

            // Get the ReceiptController and set the receipt text
            ReceiptController receiptController = loader.getController();
            receiptController.setReceiptText(receiptText);

            // Switch to the receipt screen
            Stage stage = (Stage) paymentMethodComboBox.getScene().getWindow();
            Scene receiptScene = new Scene(receiptView);
            stage.setScene(receiptScene);
            stage.setTitle("Receipt");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading the receipt screen.");
        }
    }

    // Update total income in the database
    private void updateTotalIncome(double totalAmount) {
        String insertSaleQuery = "INSERT INTO sales (sale_date, total_amount) VALUES (NOW(), ?)";
        String totalIncomeQuery = "SELECT SUM(total_amount) AS total_income FROM sales";

        try (Connection conn = DatabaseUtil.connect()) {
            // Insert the new sale into the sales table
            try (PreparedStatement stmtInsert = conn.prepareStatement(insertSaleQuery)) {
                stmtInsert.setDouble(1, totalAmount);
                stmtInsert.executeUpdate();
            }

            // Retrieve the updated total income
            try (PreparedStatement stmtSelect = conn.prepareStatement(totalIncomeQuery)) {
                var resultSet = stmtSelect.executeQuery();
                if (resultSet.next()) {
                    double totalIncome = resultSet.getDouble("total_income");
                    System.out.println("Total Income updated: $" + totalIncome);
                    // Optionally, update the UI or database with this value
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while updating total income.");
            e.printStackTrace();
        }
    }

    // Show success message
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Success");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    // Show error message
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Payment Error");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    // Handle the payment method selection event
    @FXML
    private void onPaymentMethodSelected(ActionEvent event) {
        String selectedPaymentMethod = paymentMethodComboBox.getValue();
        System.out.println("Selected Payment Method: " + selectedPaymentMethod);
    }

    // Method to navigate back to the Billing screen
    @FXML
    private void goBackToBilling(ActionEvent event) {
        try {
            Parent billingView = FXMLLoader.load(getClass().getResource("/com/example/supermarketbillingsystem/billing.fxml"));
            Scene billingScene = new Scene(billingView);
            Stage window = (Stage) paymentMethodComboBox.getScene().getWindow();
            window.setScene(billingScene);
            window.setTitle("Billing");
            window.show();
        } catch (IOException e) {
            System.err.println("Error loading the Billing screen.");
            e.printStackTrace();
        }
    }
}
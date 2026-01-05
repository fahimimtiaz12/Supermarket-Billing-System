package com.example.supermarketbillingsystem;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class TotalIncomeController {
    // Fields to store role and username
    private String userRole;
    private String username;

    // Setter method called from dashboard
    public void setRoleAndUsername(String role, String username) {
        this.userRole = role;
        this.username = username;
    }


    @FXML
    private Label totalIncomeLabel;

    // Method to update the total income label
    @FXML
    public void initialize() {
        System.out.println("Initializing Total Income controller...");
        updateTotalIncomeLabel();
    }

    // Fixed: Method signature must match FXML - accept ActionEvent parameter
    @FXML
    public void refreshTotalIncome(ActionEvent event) {
        System.out.println("Refresh button clicked!");

        // Add simple visual feedback by temporarily changing text
        totalIncomeLabel.setText("Loading...");

        // Update the label after a small delay
        javafx.util.Duration delay = javafx.util.Duration.millis(200);
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                updateTotalIncomeLabel();
                stop(); // Stop the timer after one cycle
            }
        };
        timer.start();
    }

    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/supermarketbillingsystem/dashboard.fxml"));
            Parent dashboardView = loader.load();

            // Pass the same role and username back to dashboard
            DashboardController controller = loader.getController();
            controller.setUserRole(userRole);
            controller.setCashierInfo(username, "online"); // Or the actual status if you have it

            Scene dashboardScene = new Scene(dashboardView);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(dashboardScene);
            window.setTitle("Dashboard");
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Fetch and update total income from the database
    private void updateTotalIncomeLabel() {
        System.out.println("Updating total income label...");
        double totalIncome = getTotalIncomeFromDatabase();
        DecimalFormat df = new DecimalFormat("0.00");
        totalIncomeLabel.setText("$" + df.format(totalIncome));
    }

    // Fetch total income from the database
    private double getTotalIncomeFromDatabase() {
        double totalIncome = 0.0;
        try (Connection conn = DatabaseUtil.connect()) {
            System.out.println("Connecting to database...");
            String query = "SELECT SUM(total_amount) AS total_income FROM sales";
            System.out.println("Executing query: " + query);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    totalIncome = rs.getDouble("total_income");
                    System.out.println("Total income retrieved: " + totalIncome);
                } else {
                    System.out.println("No data found in sales table");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error: " + e.getMessage());
            showError("Error fetching total income from the database.");
        }
        return totalIncome;
    }

    // Method to show an error message
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
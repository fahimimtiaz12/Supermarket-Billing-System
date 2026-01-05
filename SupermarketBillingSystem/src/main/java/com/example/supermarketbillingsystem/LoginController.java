package com.example.supermarketbillingsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField enterPasswordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button cancelButton;

    @FXML
    private CheckBox rememberpass; // Inject the CheckBox

    // Handle the 'Remember me' checkbox
    public void passwordremember(ActionEvent event) {
        boolean isChecked = rememberpass.isSelected();
        System.out.println("Remember me checkbox is selected: " + isChecked);
    }

    // Handle login button action
    public void loginButtonOnAction(ActionEvent event) {
        String username = usernameTextField.getText();
        String password = enterPasswordField.getText();

        // Fetch user from the database directly
        String userRole = getUserRole(username);

        if (userRole != null) {
            // If login is successful, update status to 'online'
            updateCashierStatusOnline(username);

            loginButton.setText("Login successful!");

            try {
                // Load the dashboard page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
                Parent dashboardView = loader.load();  // Load the Dashboard.fxml

                // Get the DashboardController and pass the user info
                DashboardController dashboardController = loader.getController();
                dashboardController.setCashierInfo(username, "online");  // Set username and status to "online"
                dashboardController.setUserRole(userRole); // Set user role

                Scene dashboardScene = new Scene(dashboardView);
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setScene(dashboardScene);
                window.setTitle("Dashboard");
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
                loginButton.setText("Error loading dashboard.");
            }
        } else {
            loginButton.setText("Invalid login. Please try again.");
        }
    }

    // Get user role from database
    private String getUserRole(String username) {
        String role = null;
        String query = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseUtil.connect()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, enterPasswordField.getText()); // This is a security concern - better to verify password separately
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                role = rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return role;
    }

    // Update cashier status to 'online'
    private void updateCashierStatusOnline(String username) {
        String query = "UPDATE users SET status = 'online' WHERE username = ?";

        try (Connection conn = DatabaseUtil.connect()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.executeUpdate();  // Update status to 'online'
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Cancel button action (close the login window)
    public void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
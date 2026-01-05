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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleManagementController {

    @FXML
    private TableView<UserRole> roleTable;
    @FXML
    private TableColumn<UserRole, String> roleNameColumn;
    @FXML
    private TableColumn<UserRole, String> descriptionColumn;
    @FXML
    private TableColumn<UserRole, Boolean> totalIncomePermissionColumn;
    @FXML
    private TableColumn<UserRole, Boolean> productManagementPermissionColumn;
    @FXML
    private TableColumn<UserRole, Boolean> billingPermissionColumn;
    @FXML
    private TableColumn<UserRole, Boolean> logoutPermissionColumn;

    @FXML
    private TextField roleNameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private CheckBox totalIncomeCheckBox;
    @FXML
    private CheckBox productManagementCheckBox;
    @FXML
    private CheckBox billingCheckBox;
    @FXML
    private CheckBox logoutCheckBox;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button backButton;

    private ObservableList<UserRole> roleData;
    private UserRole selectedRole;

    // 🔹 Current logged-in user info (from Dashboard/Login)
    private String currentUsername;
    private String currentUserRole;

    // Called from Dashboard to inject user info
    public void setUserInfo(String username, String role) {
        this.currentUsername = username;
        this.currentUserRole = role;
    }

    public void initialize() {
        // Initialize table columns
        roleNameColumn.setCellValueFactory(cellData -> cellData.getValue().roleNameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        totalIncomePermissionColumn.setCellValueFactory(cellData -> cellData.getValue().totalIncomePermissionProperty());
        productManagementPermissionColumn.setCellValueFactory(cellData -> cellData.getValue().productManagementPermissionProperty());
        billingPermissionColumn.setCellValueFactory(cellData -> cellData.getValue().billingPermissionProperty());
        logoutPermissionColumn.setCellValueFactory(cellData -> cellData.getValue().logoutPermissionProperty());

        // Load roles from DB
        loadRoles();

        // Listen for table selection
        roleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedRole = newSelection;
                populateFormFields(selectedRole);
            }
        });
    }

    private void loadRoles() {
        roleData = FXCollections.observableArrayList();
        try (Connection conn = DatabaseUtil.connect()) {
            String query = "SELECT DISTINCT role, username FROM users WHERE role IS NOT NULL ORDER BY role";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String role = rs.getString("role");
                String username = rs.getString("username");

                String permQuery = "SELECT total_income_access, product_management_access, billing_access, logout_access " +
                        "FROM users WHERE role = ? LIMIT 1";
                PreparedStatement permStmt = conn.prepareStatement(permQuery);
                permStmt.setString(1, role);
                ResultSet permRs = permStmt.executeQuery();

                boolean totalIncome = false;
                boolean productManagement = false;
                boolean billing = false;
                boolean logout = true;

                if (permRs.next()) {
                    totalIncome = permRs.getBoolean("total_income_access");
                    productManagement = permRs.getBoolean("product_management_access");
                    billing = permRs.getBoolean("billing_access");
                    logout = permRs.getBoolean("logout_access");
                }

                UserRole userRole = new UserRole(
                        role,
                        username,
                        totalIncome,
                        productManagement,
                        billing,
                        logout
                );
                roleData.add(userRole);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading roles: " + e.getMessage());
        }
        roleTable.setItems(roleData);
    }

    private void populateFormFields(UserRole role) {
        roleNameField.setText(role.getRoleName());
        descriptionField.setText(role.getDescription());
        totalIncomeCheckBox.setSelected(role.isTotalIncomeAccess());
        productManagementCheckBox.setSelected(role.isProductManagementAccess());
        billingCheckBox.setSelected(role.isBillingAccess());
        logoutCheckBox.setSelected(role.isLogoutAccess());
    }

    @FXML
    private void handleAddRole(ActionEvent event) {
        String roleName = roleNameField.getText().trim();
       // String description = descriptionField.getText().trim();

        if (roleName.isEmpty()) {
            showAlert("Validation Error", "Role name is required");
            return;
        }

        try (Connection conn = DatabaseUtil.connect()) {
            String checkQuery = "SELECT COUNT(*) FROM users WHERE role = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, roleName);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                showAlert("Duplicate Role", "Role '" + roleName + "' already exists");
                return;
            }

            String insertQuery = "INSERT INTO users (username, password, role, status, total_income_access, product_management_access, billing_access, logout_access) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, roleName + "_user");
            insertStmt.setString(2, "default123");
            insertStmt.setString(3, roleName);
            insertStmt.setString(4, "offline");
            insertStmt.setBoolean(5, totalIncomeCheckBox.isSelected());
            insertStmt.setBoolean(6, productManagementCheckBox.isSelected());
            insertStmt.setBoolean(7, billingCheckBox.isSelected());
            insertStmt.setBoolean(8, logoutCheckBox.isSelected());

            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Role '" + roleName + "' created successfully");
                clearForm();
                loadRoles();
            } else {
                showAlert("Error", "Failed to create role '" + roleName + "'");
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Error creating role: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateRole(ActionEvent event) {
        if (selectedRole == null) {
            showAlert("Selection Error", "Please select a role to update");
            return;
        }

        String roleName = roleNameField.getText().trim();
        if (roleName.isEmpty()) {
            showAlert("Validation Error", "Role name is required");
            return;
        }

        try (Connection conn = DatabaseUtil.connect()) {
            String updateQuery = "UPDATE users SET total_income_access = ?, product_management_access = ?, billing_access = ?, logout_access = ? WHERE role = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setBoolean(1, totalIncomeCheckBox.isSelected());
            stmt.setBoolean(2, productManagementCheckBox.isSelected());
            stmt.setBoolean(3, billingCheckBox.isSelected());
            stmt.setBoolean(4, logoutCheckBox.isSelected());
            stmt.setString(5, selectedRole.getRoleName());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Role '" + roleName + "' updated successfully");
                clearForm();
                loadRoles();
            } else {
                showAlert("Error", "Failed to update role '" + roleName + "'");
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Error updating role: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteRole(ActionEvent event) {
        if (selectedRole == null) {
            showAlert("Selection Error", "Please select a role to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Role");
        alert.setContentText("Are you sure you want to delete the role '" + selectedRole.getRoleName() + "'?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection conn = DatabaseUtil.connect()) {
                String deleteQuery = "DELETE FROM users WHERE role = ?";
                PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                stmt.setString(1, selectedRole.getRoleName());
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert("Success", "Role '" + selectedRole.getRoleName() + "' deleted successfully");
                    clearForm();
                    loadRoles();
                } else {
                    showAlert("Error", "Failed to delete role '" + selectedRole.getRoleName() + "'");
                }

            } catch (SQLException e) {
                showAlert("Database Error", "Error deleting role: " + e.getMessage());
            }
        }
    }

    // 🔹 FIXED: Back button now restores correct user role
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent dashboardView = loader.load();

            DashboardController dashboardController = loader.getController();
            dashboardController.setCashierInfo(currentUsername, "online");
            dashboardController.setUserRole(currentUserRole);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(new Scene(dashboardView));
            window.setTitle("Dashboard");
            window.show();
        } catch (IOException e) {
            showAlert("Error", "Error loading dashboard: " + e.getMessage());
        }
    }

    private void clearForm() {
        roleNameField.clear();
        descriptionField.clear();
        totalIncomeCheckBox.setSelected(false);
        productManagementCheckBox.setSelected(false);
        billingCheckBox.setSelected(false);
        logoutCheckBox.setSelected(false);
        selectedRole = null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

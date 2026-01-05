package com.example.supermarketbillingsystem;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserRole {
    private final StringProperty roleName;
    private final StringProperty description;
    private final BooleanProperty totalIncomeAccess;
    private final BooleanProperty productManagementAccess;
    private final BooleanProperty billingAccess;
    private final BooleanProperty logoutAccess;

    public UserRole(String roleName, String description, boolean totalIncomeAccess,
                    boolean productManagementAccess, boolean billingAccess, boolean logoutAccess) {
        this.roleName = new SimpleStringProperty(roleName);
        this.description = new SimpleStringProperty(description);
        this.totalIncomeAccess = new SimpleBooleanProperty(totalIncomeAccess);
        this.productManagementAccess = new SimpleBooleanProperty(productManagementAccess);
        this.billingAccess = new SimpleBooleanProperty(billingAccess);
        this.logoutAccess = new SimpleBooleanProperty(logoutAccess);
    }

    // Getters
    public String getRoleName() { return roleName.get(); }
    public String getDescription() { return description.get(); }
    public boolean isTotalIncomeAccess() { return totalIncomeAccess.get(); }
    public boolean isProductManagementAccess() { return productManagementAccess.get(); }
    public boolean isBillingAccess() { return billingAccess.get(); }
    public boolean isLogoutAccess() { return logoutAccess.get(); }

    // Properties
    public StringProperty roleNameProperty() { return roleName; }
    public StringProperty descriptionProperty() { return description; }
    public BooleanProperty totalIncomePermissionProperty() { return totalIncomeAccess; }
    public BooleanProperty productManagementPermissionProperty() { return productManagementAccess; }
    public BooleanProperty billingPermissionProperty() { return billingAccess; }
    public BooleanProperty logoutPermissionProperty() { return logoutAccess; }

    // Setters
    public void setRoleName(String roleName) { this.roleName.set(roleName); }
    public void setDescription(String description) { this.description.set(description); }
    public void setTotalIncomeAccess(boolean totalIncomeAccess) { this.totalIncomeAccess.set(totalIncomeAccess); }
    public void setProductManagementAccess(boolean productManagementAccess) { this.productManagementAccess.set(productManagementAccess); }
    public void setBillingAccess(boolean billingAccess) { this.billingAccess.set(billingAccess); }
    public void setLogoutAccess(boolean logoutAccess) { this.logoutAccess.set(logoutAccess); }
}
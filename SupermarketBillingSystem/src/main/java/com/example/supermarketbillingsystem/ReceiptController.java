package com.example.supermarketbillingsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;

public class ReceiptController {

    @FXML
    private TextArea receiptTextArea;

    // Method to set the receipt text in the TextArea
    public void setReceiptText(String receiptText) {
        receiptTextArea.setText(receiptText);
    }

    // Method to handle the Back button click and navigate back to the Billing screen
    @FXML
    private void goBackToBilling(ActionEvent event) {
        try {
            // Load the Billing screen (billing.fxml)
            Parent billingView = FXMLLoader.load(getClass().getResource("/com/example/supermarketbillingsystem/billing.fxml"));

            // Get the current stage and change the scene to the Billing screen
            Scene billingScene = new Scene(billingView);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(billingScene);
            window.setTitle("Billing");
            window.show();
        } catch (IOException e) {
            System.err.println("Error loading the Billing screen.");
            e.printStackTrace();
        }
    }
}
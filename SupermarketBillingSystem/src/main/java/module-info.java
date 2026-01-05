module com.example.supermarketbillingsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.logging;
    requires java.desktop;

    opens com.example.supermarketbillingsystem to javafx.fxml;

    exports com.example.supermarketbillingsystem;
}

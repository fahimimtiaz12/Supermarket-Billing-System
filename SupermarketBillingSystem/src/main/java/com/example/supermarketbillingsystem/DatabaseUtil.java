package com.example.supermarketbillingsystem;

import java.sql.*;

public class DatabaseUtil {

    // Database connection URL, username, and password
    private static final String DB_URL = "jdbc:mysql://localhost:3306/supermarket_db";
    private static final String USER = "root";
    private static final String PASS = "123456";  // Replace with your actual password

    // Connect to the database
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // Get product by product code from the database
    public static Product getProductByCode(String productCode) {
        Product product = null;
        String query = "SELECT * FROM products WHERE product_code = ?";
        try (Connection conn = connect()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String category = rs.getString("category");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");  // Ensure this is getting the correct value
                product = new Product(name, category, productCode, quantity, price);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

}

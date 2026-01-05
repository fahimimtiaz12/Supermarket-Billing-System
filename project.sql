USE supermarket_db;
-- Create users table for authentication
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Store hashed passwords in production
    role VARCHAR(255) NOT NULL
);

-- Create products table for inventory
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL
);
-- Sample Users for authentication
INSERT INTO users (username, password, role) 
VALUES 
    ('admin', 'adminpassword', 'admin'),  -- Admin user
    ('cashier', 'cashierpassword', 'cashier'); -- Regular user

-- Sample Products for the inventory
INSERT INTO products (name, category, price, quantity) 
VALUES 
    ('Apple', 'Fruits', 3.50, 100),
    ('Banana', 'Fruits', 2.00, 150),
    ('Carrot', 'Vegetables', 1.20, 200),
    ('Tomato', 'Vegetables', 2.50, 120),
    ('Milk', 'Dairy', 1.60, 80),
    ('Cheese', 'Dairy', 5.00, 50),
    ('Rice', 'Grains', 1.80, 200),
    ('Flour', 'Grains', 2.30, 180);
    
    SELECT * FROM users;



# Supermarket-Billing-System
A Supermarket Billing System built with JavaFX and MySQL is a desktop application that helps supermarket staff manage sales, customers, and invoices in a faster and more organized way. It provides a graphical interface for cashiers to search products, add them to a bill, calculate totals, and store transaction data safely in a database.


A desktop **Supermarket Billing System** built with JavaFX and MySQL.  
The application helps supermarket staff manage products, handle billing at the counter, and store sales records safely in a database.

---

## Features

- **Product management**  
  - Store product information such as name, price, stock quantity, and category in a MySQL database.  
  - Load products into the UI so cashiers can quickly search and select items.

- **Billing interface**  
  - JavaFX-based window to add items to the current bill using tables, input fields, and buttons.  
  - Update quantities or remove items from the cart before finalizing the bill.

- **Automatic calculations**  
  - Compute line totals, subtotal, discounts (if any), taxes, and final payable amount in real time.  
  - Reduce manual work for cashiers and minimize calculation mistakes.

- **Sales records**  
  - Save each completed bill to the database as a sale entry.  
  - Use stored data for basic reporting, daily sales checking, and future analysis.

- **User accounts (if implemented)**  
  - Store user login data in the `users` table.  
  - Optionally restrict access so only registered users can use the system.

---

## Tech Stack

- **Language:** Java  
- **UI Framework:** JavaFX  
- **Database:** MySQL (`supermarket_db`)  
- **Persistence:** JDBC  
- **Build Tool:** Maven (project includes `pom.xml`)  

---

## Database Design

**Database name:** `supermarket_db`  

**Main tables:**

- `products`  
  - Stores product details such as ID, name, price, and available quantity.  
- `sales`  
  - Stores each bill / transaction, including date, total amount, and related information.  
- `users`  
  - Stores application users (cashiers/admins) for login and access control.

> Note: Exact column names and types depend on your implementation; adjust this section if needed.

---

## How to Run

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-username/SupermarketBillingSystem.git
   cd SupermarketBillingSystem
   ```

2. **Set up the MySQL database**

   1. Create the database:

      ```sql
      CREATE DATABASE supermarket_db;
      ```

   2. Create the required tables (example names):

      ```sql
      USE supermarket_db;

      -- Products table (example)
      CREATE TABLE products (
          id INT PRIMARY KEY AUTO_INCREMENT,
          name VARCHAR(100),
          price DECIMAL(10,2),
          quantity INT
      );

      -- Users table (example)
      CREATE TABLE users (
          id INT PRIMARY KEY AUTO_INCREMENT,
          username VARCHAR(50),
          password VARCHAR(100),
          role VARCHAR(20)
      );

      -- Sales table (example)
      CREATE TABLE sales (
          id INT PRIMARY KEY AUTO_INCREMENT,
          datetime DATETIME,
          total_amount DECIMAL(10,2)
      );
      ```

   3. Insert some sample data into `products` (and `users` if you have login) so you can test the system.

   4. In the Java project, update the database URL, username, and password in your connection/config class, for example:

      ```java
      String url = "jdbc:mysql://localhost:3306/supermarket_db";
      String user = "root";
      String password = "your_password";
      ```

3. **Build and run (Maven + JavaFX)**

   - Open the project in IntelliJ IDEA (or another IDE) as a Maven project.  
   - Let Maven download all dependencies defined in `pom.xml`.  
   - Make sure JavaFX is correctly configured (VM options / module path if needed).  
   - Run the main JavaFX application class (for example `Main` or `App`) to start the UI.

   If you use Maven to run directly:

   ```bash
   mvn clean install
   mvn javafx:run   # or your configured goal
   ```

   (Adjust the command if you use a different plugin or configuration.)

4. **Use the application**

   - Log in with a user from the `users` table (if login is implemented).  
   - Search/select products, add them to the bill, adjust quantities, and generate the final bill.  
   - Confirm the bill so the sale is stored in the `sales` table.

---

## Project Structure (high-level)

- `src/main/java`  
  - JavaFX controllers and UI logic  
  - Database connection and DAO classes  
  - Main application entry point
- `src/main/resources`  
  - FXML files (if used)  
  - Stylesheets and other resources
- `pom.xml`  
  - Maven dependencies and JavaFX/MySQL configuration

---

## Future Improvements

- Add role-based access (admin vs cashier).  
- Add more detailed reporting (daily/weekly/monthly sales).  
- Export bills or sales reports as PDF/Excel.  
- Improve UI design and add input validation in all forms.

---

## License
MIT License

You only need to change the repository URL, main class name, database user/password, and any details that differ from your real implementation before committing this file.

[1](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/155715331/d607120b-8f7b-47db-a1c5-f5ee884bf93f/StudentID-Name-Experiment-Report1-Basic-Operations-of-Sequence-List.docx)
[2](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/images/155715331/bb771f2d-4a74-4a15-aaca-c631c4c82481/image.jpg)
[3](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/images/155715331/46a347ae-b19a-4910-9899-a626ed39ec7d/image.jpg)

# Java Point-of-Sale (POS) System

A comprehensive desktop Point-of-Sale (POS) application built with Java and Swing for the GUI, and connected to a MySQL database for data persistence. This project simulates a real-world retail environment, managing sales, inventory, and customer transactions.

## Features

- **Dashboard Navigation:** A modern, icon-driven main menu for easy navigation between modules.
- **Advanced POS Interface:**
    - Browse products by category.
    - Search for products by name or reference number.
    - Visual product grid with images for quick selection.
    - Interactive cart with in-line quantity editing and item removal.
- **Full Inventory Management:**
    - A powerful management panel to add, edit, and delete products.
    - Includes fields for name, brand, reference number, price, quantity, and category.
    - Features an image uploader that copies product images to the project resources.
- **Checkout Process:**
    - A professional, multi-step checkout dialog.
    - Supports both **Cash** and **Card** payments.
    - For cash payments, it calculates and displays the change due.
- **Sales Reporting:**
    - A master-detail view showing a list of all historical sales.
    - Clicking a sale displays all the specific items sold in that transaction.
- **Database Integration:**
    - All product, inventory, and sales data is stored and managed in a MySQL database.
    - Uses JDBC for robust database connectivity.

## Technologies Used

- **Language:** Java
- **GUI:** Java Swing
- **Database:** MySQL
- **Connectivity:** JDBC (MySQL Connector/J)
- **IDE:** IntelliJ IDEA

## Setup and Installation

1.  **Prerequisites:**
    - Java Development Kit (JDK) 20 or newer.
    - A local MySQL server (e.g., via XAMPP).
    - An IDE like IntelliJ IDEA.

2.  **Database Setup:**
    - Open phpMyAdmin and create a new database named `pos_system`.
    - Go to the "SQL" tab and execute the complete SQL script provided in the project to create all tables and insert sample data.

3.  **Configuration:**
    - Open the `DatabaseConnection.java` file.
    - Update the `URL`, `USER`, and `PASSWORD` constants to match your MySQL setup.

4.  **Run the Application:**
    - Open the project in IntelliJ IDEA.
    - Ensure the MySQL Connector/J JAR file is added as a project dependency.
    - Run the `Main.java` file to launch the application dashboard.

## Screenshots

*(Here, you can add screenshots of your application. You can upload them to the GitHub repository and link them like this:)*

**Dashboard**
![Dashboard](path/to/your/screenshot.png)

**Point of Sale Screen**
![POS Screen](path/to/your/screenshot.png)

**Inventory Management**
![Inventory Screen](path/to/your/screenshot.png)
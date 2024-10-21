package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.sql.*;

// BIAR BISA NGEBUAT UI APLIKASI DESKTOPNYA PAKE EXTENDS KE APPLICATION JAVA FX
public class App extends Application {

    // BUAT TABLE VIEW DATA SQL
    private final TableView<Shoe> tableView = new TableView<>();

    // BUAT FORM ID, NAME, DESC + SPINNER PRICE
    private final TextField shoeNameField = new TextField();
    private final Spinner<Integer> shoePriceField = new Spinner<>(0, 1000000, 300000);
    private final TextField shoeDescriptionField = new TextField();
    private final TextField shoeIdField = new TextField();

    // BUAT KONEKSI KE DB
    private Connection connection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize database connection
        initDatabaseConnection();

        // Create UI components
        VBox root = new VBox();
        Label titleLabel = new Label("Insert Shoe");

        // Shoe ID (not editable)
        Label shoeIdLabel = new Label("Shoe ID");
        shoeIdField.setEditable(false);
        generateShoeId();  // Auto-generate the shoe ID

        // Shoe Name
        Label shoeNameLabel = new Label("Shoe Name");

        // Shoe Price
        Label shoePriceLabel = new Label("Shoe Price");

        // Shoe Description
        Label shoeDescriptionLabel = new Label("Shoe Description");

        // Submit button
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleSubmit());

        // Layout form components
        GridPane formLayout = new GridPane();
        formLayout.add(titleLabel, 0, 0);
        formLayout.add(shoeIdLabel, 0, 1);
        formLayout.add(shoeIdField, 1, 1);
        formLayout.add(shoeNameLabel, 0, 2);
        formLayout.add(shoeNameField, 1, 2);
        formLayout.add(shoePriceLabel, 0, 3);
        formLayout.add(shoePriceField, 1, 3);
        formLayout.add(shoeDescriptionLabel, 0, 4);
        formLayout.add(shoeDescriptionField, 1, 4);
        formLayout.add(submitButton, 1, 5);

        // Initialize the table view
        initializeTable();

        // Add components to root layout
        root.getChildren().addAll(tableView, formLayout);

        // Create scene and set stage
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Just DU It");
        primaryStage.show();
    }

    // Initialize PostgreSQL database connection
    private void initDatabaseConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/shoe_database"; // Adjust the database name and port
        String user = "postgres"; // Replace with your PostgreSQL username
        String password = "tuyul123"; // Replace with your PostgreSQL password
        connection = DriverManager.getConnection(url, user, password);
    }

    // Generate the next shoe ID
    private void generateShoeId() {
        try {
            String query = "SELECT shoe_id FROM shoes ORDER BY shoe_id DESC LIMIT 1";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String lastId = rs.getString("shoe_id").substring(2);
                int nextId = Integer.parseInt(lastId) + 1;
                shoeIdField.setText("SH" + String.format("%03d", nextId));
            } else {
                shoeIdField.setText("SH001");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Initialize the table view
    private void initializeTable() {
        TableColumn<Shoe, String> idColumn = new TableColumn<>("Shoe ID");
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getShoeId()));

        TableColumn<Shoe, String> nameColumn = new TableColumn<>("Shoe Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getShoeName()));

        TableColumn<Shoe, Integer> priceColumn = new TableColumn<>("Shoe Price");
        priceColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getShoePrice()).asObject());

        TableColumn<Shoe, String> descriptionColumn = new TableColumn<>("Shoe Description");
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getShoeDescription()));

        tableView.getColumns().addAll(idColumn, nameColumn, priceColumn, descriptionColumn);

        // Load data from database
        loadShoeData();
    }

    // Load shoe data from the database
    private void loadShoeData() {
        try {
            String query = "SELECT shoe_id, shoe_name, shoe_price, shoe_description FROM shoes";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String shoeId = rs.getString("shoe_id");
                String shoeName = rs.getString("shoe_name");
                int shoePrice = rs.getInt("shoe_price");
                String shoeDescription = rs.getString("shoe_description");
                tableView.getItems().add(new Shoe(shoeId, shoeName, shoePrice, shoeDescription));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Handle form submission
    private void handleSubmit() {
        String shoeId = shoeIdField.getText();
        String shoeName = shoeNameField.getText();
        int shoePrice = shoePriceField.getValue();
        String shoeDescription = shoeDescriptionField.getText();

        // Validation
        if (shoeName.isEmpty() || shoeName.length() > 30) {
            showAlert("Error", "Shoe Name must be filled and less than 30 characters.");
            return;
        }
        if (shoeDescription.isEmpty() || shoeDescription.length() > 255) {
            showAlert("Error", "Shoe Description must be filled and less than 255 characters.");
            return;
        }
        if (shoePrice <= 0) {
            showAlert("Error", "Shoe Price must be greater than 0.");
            return;
        }

        // Insert shoe data into database
        try {
            String query = "INSERT INTO shoes (shoe_id, shoe_name, shoe_price, shoe_description) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, shoeId);
            stmt.setString(2, shoeName);
            stmt.setInt(3, shoePrice);
            stmt.setString(4, shoeDescription);
            stmt.executeUpdate();

            // Refresh shoe ID and clear form
            generateShoeId();
            shoeNameField.clear();
            shoePriceField.getValueFactory().setValue(300000);
            shoeDescriptionField.clear();

            // Refresh table data
            loadShoeData();

            showAlert("Success", "Shoe added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add shoe. Please try again.");
        }
    }

    // Show a popup alert
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // SHOW ALERT IN JFX APPLICATION
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        // Close the database connection when the app is closed
        if (connection != null) {
            connection.close();
        }
        super.stop();
    }
}

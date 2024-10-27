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

public class App extends Application {
    private final TableView<Shoe> tableView = new TableView<>();
    private final TextField shoeNameField = new TextField();
    private final Spinner<Integer> shoePriceField = new Spinner<>(0, 1000000, 300000);
    private final TextField shoeDescriptionField = new TextField();
    private final TextField shoeIdField = new TextField();

    private String lastSelectedShoeId = null; // To keep track of the last selected shoe ID

    private Connection connection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initDatabaseConnection();

        VBox root = new VBox();
        Label titleLabel = new Label("Insert Shoe");

        Label shoeIdLabel = new Label("Shoe ID");
        shoeIdField.setEditable(false);
        generateShoeId();

        Label shoeNameLabel = new Label("Shoe Name");
        Label shoePriceLabel = new Label("Shoe Price");
        Label shoeDescriptionLabel = new Label("Shoe Description");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleSubmit());

        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> handleUpdate());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> handleDelete());

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
        formLayout.add(submitButton, 0, 5);
        formLayout.add(updateButton, 1, 5);
        formLayout.add(deleteButton, 2, 5);

        initializeTable();

        // Handle row selection and reset form if the same row is clicked again
        tableView.setOnMouseClicked(e -> handleRowSelect());

        root.getChildren().addAll(tableView, formLayout);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Just DU It");
        primaryStage.show();
    }

    private void initDatabaseConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/shoe_database";
        String user = "postgres";
        String password = "tuyul123";
        connection = DriverManager.getConnection(url, user, password);
    }

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
        loadShoeData();
    }

    private void loadShoeData() {
        tableView.getItems().clear();
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

    private void handleSubmit() {
        String shoeId = shoeIdField.getText();
        String shoeName = shoeNameField.getText();
        int shoePrice = shoePriceField.getValue();
        String shoeDescription = shoeDescriptionField.getText();

        if (!validateFields(shoeId, shoeName, shoeDescription, shoePrice)) return;

        try {
            String query = "INSERT INTO shoes (shoe_id, shoe_name, shoe_price, shoe_description) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, shoeId);
            stmt.setString(2, shoeName);
            stmt.setInt(3, shoePrice);
            stmt.setString(4, shoeDescription);
            stmt.executeUpdate();

            resetForm(); // Reset the form after submission
            loadShoeData();
            showAlert("Success", "Shoe added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add shoe. Please try again.");
        }
    }

    private void handleUpdate() {
        String shoeId = shoeIdField.getText();
        String shoeName = shoeNameField.getText();
        int shoePrice = shoePriceField.getValue();
        String shoeDescription = shoeDescriptionField.getText();

        if (!validateFields(shoeId, shoeName, shoeDescription, shoePrice)) return;

        try {
            String query = "UPDATE shoes SET shoe_name = ?, shoe_price = ?, shoe_description = ? WHERE shoe_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, shoeName);
            stmt.setInt(2, shoePrice);
            stmt.setString(3, shoeDescription);
            stmt.setString(4, shoeId);
            stmt.executeUpdate();
            loadShoeData();
            showAlert("Success", "Shoe updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update shoe. Please try again.");
        }
    }

    private void handleDelete() {
        String shoeId = shoeIdField.getText();
        if (shoeId.isEmpty()) {
            showAlert("Error", "Shoe ID must not be empty.");
            return;
        }

        try {
            String query = "DELETE FROM shoes WHERE shoe_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, shoeId);
            stmt.executeUpdate();
            loadShoeData();
            generateShoeId();
            shoeNameField.clear();
            shoePriceField.getValueFactory().setValue(300000);
            shoeDescriptionField.clear();
            showAlert("Success", "Shoe deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to delete shoe. Please try again.");
        }
    }

    private void handleRowSelect() {
        Shoe selectedShoe = tableView.getSelectionModel().getSelectedItem();
        if (selectedShoe != null) {
            // Check if the same row is clicked again
            if (selectedShoe.getShoeId().equals(lastSelectedShoeId)) {
                resetForm(); // Reset form if the same row is clicked again
            } else {
                shoeIdField.setText(selectedShoe.getShoeId());
                shoeNameField.setText(selectedShoe.getShoeName());
                shoePriceField.getValueFactory().setValue(selectedShoe.getShoePrice());
                shoeDescriptionField.setText(selectedShoe.getShoeDescription());
                lastSelectedShoeId = selectedShoe.getShoeId(); // Update the last selected shoe ID
            }
        } else {
            resetForm(); // Reset form if clicked on an empty area
        }
    }

    private void resetForm() {
        generateShoeId(); // Generate new shoe ID
        shoeNameField.clear();
        shoePriceField.getValueFactory().setValue(300000);
        shoeDescriptionField.clear();
        lastSelectedShoeId = null; // Reset the last selected shoe ID
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean validateFields(String shoeId, String shoeName, String shoeDescription, int shoePrice) {
        if (shoeId.isEmpty()) {
            showAlert("Error", "Shoe ID must not be empty.");
            return false;
        }
        if (shoeName.isEmpty() || shoeName.length() > 30) {
            showAlert("Error", "Shoe Name must be filled and less than 30 characters.");
            return false;
        }
        if (shoeDescription.isEmpty() || shoeDescription.length() > 255) {
            showAlert("Error", "Shoe Description must be filled and less than 255 characters.");
            return false;
        }
        if (shoePrice <= 0) {
            showAlert("Error", "Shoe Price must be greater than 0.");
            return false;
        }
        return true;
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

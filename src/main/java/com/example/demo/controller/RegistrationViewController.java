package com.example.demo.controller;

import com.example.demo.model.DatabaseConnectionSingleton;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrationViewController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private void handleRegisterSubmitButton() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Пожалуйста, заполните все поля.");
            return;
        }

        registerUser(username, password);
        showAlert("Успех", "Пользователь успешно зарегистрирован!");
        clearFields();
    }

    private boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection connection = DatabaseConnectionSingleton.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
            return true; // Возвращает true, если регистрация успешна

        } catch (SQLException e) {
            System.out.println("Ошибка при регистрации пользователя: " + e.getMessage());
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }
}

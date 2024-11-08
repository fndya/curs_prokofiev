package com.example.demo.controller;

import com.example.demo.model.DatabaseConnectionSingleton;
import com.example.demo.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginViewController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginSubmitButton;

    private final UserController userController = new UserController();

    @FXML
    public void handleLoginSubmitButton() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = validateLogin(username, password);
        if (user != null) {
            System.out.println("Login successful!");

            // Закрытие окна логина
            Stage stage = (Stage) loginSubmitButton.getScene().getWindow();
            stage.close();

            // Открытие окна выбора таблиц с передачей объекта User
            openTableSelectionWindow(user);

        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    // Метод для проверки логина и пароля и возврата пользователя с ролью
    private User validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DatabaseConnectionSingleton.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getInt("id"),
                            resultSet.getString("username"),
                            resultSet.getString("password"),
                            resultSet.getString("role")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при проверке данных входа: " + e.getMessage());
        }
        return null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openTableSelectionWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UserMainMenu.fxml"));
            Parent root = loader.load();

            TableSelectionController controller = loader.getController();
            controller.setUserRole(user.getRole()); // Передаём роль пользователя

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

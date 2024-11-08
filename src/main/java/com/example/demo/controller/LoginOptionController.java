package com.example.demo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginOptionController {

    @FXML
    private void handleLoginButton(ActionEvent event) {
        // Открытие нового окна
        openWindow("/view/Login.fxml");

        // Закрытие текущего окна
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRegisterButton(ActionEvent event) {
        // Открытие нового окна
        openWindow("/view/Registration.fxml");

        // Закрытие текущего окна
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }


    private void openWindow(String fxmlPath) {
        try {
            // Загружаем файл FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Открываем новое окно
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Не удалось загрузить файл FXML: " + fxmlPath);
        }
    }
}

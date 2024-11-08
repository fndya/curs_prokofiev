package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {
    ///
    @Override
    public void start(Stage stage) {
        try {
            URL fxmlLocation = getClass().getResource("/view/LoginOption.fxml");
            if (fxmlLocation == null) {
                System.out.println("FXML file not found!");
            } else {
                Parent root = FXMLLoader.load(fxmlLocation);
                stage.setTitle("МебельТочкаРу");
                stage.setScene(new Scene(root, 600, 400));
                stage.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }}
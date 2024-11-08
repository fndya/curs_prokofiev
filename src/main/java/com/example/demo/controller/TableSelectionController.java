package com.example.demo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class TableSelectionController {

    private String role;


    @FXML
    private void viewStore(ActionEvent event) {
        openTableWindow("store");
    }


    @FXML
    public void viewFurnitureItem() {
        openTableWindow("furniture_item");
    }


    @FXML
    public void viewFurnitureLine() {
        openTableWindow("furniture_line");
    }


    @FXML
    public void viewComponent() {
        openTableWindow("component");
    }


    @FXML
    public void viewComponentSet() {
        openTableWindow("component_set");
    }

    @FXML
    public void viewOrders() {
        openTableWindow("orders");
    }

    @FXML
    public void viewOrderDetails(ActionEvent actionEvent) {
        openTableWindow("order_details");
    }


    private void openTableWindow(String tableName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UniversalTableView.fxml"));
            Parent root = loader.load();

            UniversalTableController controller = loader.getController();
            try {
                controller.setTableName(tableName);  // Устанавливаем имя таблицы для загрузки данных
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            controller.setRole(role);  // Устанавливаем роль пользователя

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setUserRole(String role) {
        this.role = role;
    }



}

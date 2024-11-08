package com.example.demo.controller;

import com.example.demo.model.DatabaseConnectionSingleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

public class UniversalTableController {

    @FXML
    private TableView<Map<String, Object>> tableView;

    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private String tableName;
    private String role;

    public void setTableName(String tableName) throws SQLException {
        this.tableName = tableName;
        loadTableData(tableName);  // Загружаем все данные
    }

    public void loadTableData(String tableName) throws SQLException {
        if (tableName == null || tableName.isEmpty()) {
            System.out.println("Error: Table name is null or empty!");
            return;
        }

        System.out.println("Loading data for table: " + tableName);

        List<Map<String, Object>> data = DatabaseConnectionSingleton.getInstance().getDataForTableAsMap(tableName);

        tableView.getColumns().clear();

        if (!data.isEmpty()) {
            Map<String, Object> firstRow = data.get(0);

            for (String columnName : firstRow.keySet()) {
                TableColumn<Map<String, Object>, Object> column = new TableColumn<>(columnName);
                column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().get(columnName)));
                tableView.getColumns().add(column);
            }

            ObservableList<Map<String, Object>> observableData = FXCollections.observableArrayList();
            observableData.addAll(data);
            tableView.setItems(observableData);

            for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
                column.setMinWidth(100);
                column.setMaxWidth(500);
            }
        } else {
            System.out.println("No data found for table: " + tableName);
        }
    }

    private List<String> getColumnsForTable(String tableName) {
        List<String> columns = new ArrayList<>();
        String query = "SELECT column_name FROM information_schema.columns WHERE table_name = ?";

        try (Connection conn = DatabaseConnectionSingleton.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                columns.add(rs.getString("column_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return columns;
    }

    private String getIdColumnNameForTable(String tableName) {
        switch (tableName) {
            case "store":
                return "store_id";
            case "orders":
                return "order_id";
            case "furniture_line":
                return "furniture_line_id";
            case "furniture_item":
                return "furniture_item_id";
            case "component":
                return "component_id";
            case "component_set":
                return "component_set_id";
            case "order_details":
                return "order_details_id";
            default:
                return "id";
        }
    }

    @FXML
    private void handleBackToMenu() {
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAddRecord() {
        Map<String, Object> newRecord = getUserInputForRecord(null);

        if (newRecord != null) {
            addRecord(newRecord);
            try {
                loadTableData(tableName);
                showAlert("Успех", "Запись успешно добавлена!");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Ошибка", "Не удалось обновить таблицу.");
            }
        } else {
            showAlert("Ошибка", "Запись не была добавлена.");
        }
    }

    @FXML
    private void handleUpdateRecord() {
        Map<String, Object> selectedRecord = tableView.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showAlert("Ошибка", "Запись для редактирования не выбрана.");
            return;
        }

        Map<String, Object> updatedRecord = getUserInputForRecord(selectedRecord);
        if (updatedRecord != null) {
            if (isValidUpdate(selectedRecord, updatedRecord)) {
                int id = (int) selectedRecord.get(getIdColumnNameForTable(tableName));
                updateRecord(id, updatedRecord);
                try {
                    loadTableData(tableName);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Ошибка", "Не удалось обновить таблицу.");
                }
            } else {
                showAlert("Ошибка", "Редактирование записи нарушает целостность данных.");
            }
        }
    }

    private boolean isValidUpdate(Map<String, Object> oldRecord, Map<String, Object> updatedRecord) {
        String idColumnName = getIdColumnNameForTable(tableName);
        if (updatedRecord.containsKey(idColumnName) &&
                !updatedRecord.get(idColumnName).equals(oldRecord.get(idColumnName))) {
            return false;
        }
        return true;
    }

    private void updateButtonStates() {
        if ("ADMIN".equals(role)) {
            addButton.setDisable(false);
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
        } else {
            addButton.setDisable(true);
            updateButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    @FXML
    private void handleDeleteRecord() {
        Map<String, Object> selectedRecord = tableView.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showAlert("Ошибка", "Запись для удаления не выбрана.");
            return;
        }

        String idColumnName = getIdColumnNameForTable(tableName);
        if (!selectedRecord.containsKey(idColumnName)) {
            showAlert("Ошибка", "Не найден идентификатор записи.");
            return;
        }

        int id = (Integer) selectedRecord.get(idColumnName);

        if (confirmAction("Подтверждение удаления", "Вы уверены, что хотите удалить эту запись?")) {
            deleteRecord(id, idColumnName);
            try {
                loadTableData(tableName);
            } catch (SQLException e) {
                showAlert("Ошибка", "Не удалось обновить данные таблицы.");
                e.printStackTrace();
            }
        }
    }

    private List<String> getAutoIncrementColumns(String tableName) {
        List<String> autoIncrementColumns = new ArrayList<>();
        try (Connection conn = DatabaseConnectionSingleton.getInstance().getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {

            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String isAutoIncrement = rs.getString("IS_AUTOINCREMENT");

                if ("YES".equalsIgnoreCase(isAutoIncrement)) {
                    autoIncrementColumns.add(columnName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return autoIncrementColumns;
    }

    @FXML
    public void initialize() {
        try {
            loadTableData(tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRole(String role) {
        this.role = role;
        updateButtonStates();
    }

    public void addRecord(Map<String, Object> newRecord) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();

        newRecord.forEach((key, value) -> {
            columns.append(key).append(",");
            values.append("?,");

            params.add(value != null ? value : null);
        });

        String query = "INSERT INTO " + tableName + " (" + columns.deleteCharAt(columns.length() - 1) +
                ") VALUES (" + values.deleteCharAt(values.length() - 1) + ")";

        try (Connection conn = DatabaseConnectionSingleton.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRecord(int id, Map<String, Object> updatedData) {
        StringBuilder setClause = new StringBuilder();
        List<Object> params = new ArrayList<>();

        updatedData.forEach((key, value) -> {
            setClause.append(key).append(" = ?,");
            params.add(value != null ? value : null);
        });

        String query = "UPDATE " + tableName + " SET " + setClause.deleteCharAt(setClause.length() - 1) +
                " WHERE " + getIdColumnNameForTable(tableName) + " = ?";

        try (Connection conn = DatabaseConnectionSingleton.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            stmt.setInt(params.size() + 1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRecord(int id, String idColumnName) {
        String query = "DELETE FROM " + tableName + " WHERE " + idColumnName + " = ?";

        try (Connection conn = DatabaseConnectionSingleton.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> getUserInputForRecord(Map<String, Object> existingRecord) {
        List<String> columnNames = getColumnsForTable(tableName);
        List<String> autoIncrementColumns = getAutoIncrementColumns(tableName);
        Map<String, Object> userInput = new HashMap<>();

        for (String column : columnNames) {
            if (autoIncrementColumns.contains(column)) {
                continue;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Введите значение");
            dialog.setHeaderText("Введите значение для " + column);

            if (existingRecord != null && existingRecord.containsKey(column)) {
                dialog.setContentText("Текущее значение: " + existingRecord.get(column) + ". Новое значение:");
            } else {
                dialog.setContentText("Введите значение:");
            }

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                userInput.put(column, result.get());
            } else {
                return null;
            }
        }
        return userInput;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmAction(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

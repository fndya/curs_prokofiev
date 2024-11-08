package com.example.demo.model;

import java.sql.*;
import java.util.*;

public class DatabaseConnectionSingleton {

    private static DatabaseConnectionSingleton instance;
    private Connection connection;

    private DatabaseConnectionSingleton() {
        try {
            // Подключение к базе данных (обновите строку подключения)
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/furniture_factory", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseConnectionSingleton getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionSingleton();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/furniture_factory", "root", "");  // переподключение
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Логирование ошибки
        }
        return connection;
    }


    public List<Map<String, Object>> getDataForTableAsMap(String tableName) throws SQLException {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }

        System.out.println("Table name: " + tableName);  // Логируем имя таблицы для диагностики

        List<Map<String, Object>> result = new ArrayList<>();
        String query = "SELECT * FROM " + tableName;

        try (Statement stmt = DatabaseConnectionSingleton.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                result.add(row);
            }
        }
        return result;
    }





}

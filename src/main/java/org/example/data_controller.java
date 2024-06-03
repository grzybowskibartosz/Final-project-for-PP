package org.example;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class data_controller {
    private static final String DB_URL = "jdbc:sqlite:C:\\Users\\barto\\IdeaProjects\\final_project_v1\\identifier.sqlite";

    public void testConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                System.out.println("Połączenie z bazą danych jest udane.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public data_controller() {
        File dbFile = new File("C:\\Users\\barto\\IdeaProjects\\final_project_v1\\identifier.sqlite");
        System.out.println("Ścieżka do bazy danych: " + dbFile.getAbsolutePath());
        if (dbFile.exists()) {
            System.out.println("Plik bazy danych istnieje.");
        } else {
            System.out.println("Plik bazy danych nie istnieje.");
        }
        createTable();
    }

    private void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS currency_rates (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "currency TEXT NOT NULL, " +
                "rate REAL NOT NULL)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertCurrencyRates(JsonObject jsonObject) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String insertSQL = "INSERT INTO currency_rates (currency, rate) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);

            JsonObject rates = jsonObject.getAsJsonObject("rates");
            for (Map.Entry<String, JsonElement> entry : rates.entrySet()) {
                String currency = entry.getKey();
                double rate = entry.getValue().getAsDouble();

                try {
                    pstmt.setString(1, currency);
                    pstmt.setDouble(2, rate);
                    pstmt.addBatch();
                } catch (SQLException e) {
                    System.out.println("Błąd podczas wstawiania danych: " + e.getMessage());
                }
            }

            pstmt.executeBatch();
        } catch (SQLException e) {
            System.out.println("Błąd podczas wstawiania danych: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
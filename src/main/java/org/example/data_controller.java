package org.example;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class data_controller {
    private static final String DB_URL = "jdbc:sqlite:C:\\Users\\barto\\IdeaProjects\\final_project_v1\\identifier.sqlite";

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

    public void testConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                System.out.println("Połączenie z bazą danych jest udane.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS currency_rates (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "currency TEXT NOT NULL UNIQUE, " +
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
            String insertSQL = "INSERT OR REPLACE INTO currency_rates (currency, rate) VALUES (?, ?)";
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

    public boolean deleteCurrencyRate(String currency) {
        String deleteSQL = "DELETE FROM currency_rates WHERE currency = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setString(1, currency);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Usunięto walutę: " + currency);
                return true;
            } else {
                System.out.println("Nie znaleziono waluty: " + currency);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas usuwania danych: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean addCurrencyRate(String currency, double rate) {
        String insertSQL = "INSERT OR REPLACE INTO currency_rates (currency, rate) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, currency);
            pstmt.setDouble(2, rate);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Dodano walutę: " + currency);
                return true;
            } else {
                System.out.println("Nie udało się dodać waluty: " + currency);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas dodawania danych: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String searchCurrencyRate(String currency) {
        String querySQL = "SELECT rate FROM currency_rates WHERE currency = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            pstmt.setString(1, currency);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double rate = rs.getDouble("rate");
                return currency + ": " + rate;
            } else {
                return "Currency not found.";
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas wyszukiwania danych: " + e.getMessage());
            e.printStackTrace();
            return "Error searching for currency.";
        }
    }

    public boolean clearAllData() {
        String clearSQL = "DELETE FROM currency_rates";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(clearSQL)) {
            pstmt.executeUpdate();
            System.out.println("Wyczyszczono wszystkie dane z tabeli currency_rates.");
            return true;
        } catch (SQLException e) {
            System.out.println("Błąd podczas czyszczenia danych: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllCurrencyRates() {
        List<String> currencies = new ArrayList<>();
        String querySQL = "SELECT currency, rate FROM currency_rates";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(querySQL)) {
            while (rs.next()) {
                String currency = rs.getString("currency");
                double rate = rs.getDouble("rate");
                currencies.add(currency + ": " + rate);
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas pobierania danych: " + e.getMessage());
            e.printStackTrace();
        }
        return currencies;
    }

    public double getCurrencyRate(String currency) {
        String querySQL = "SELECT rate FROM currency_rates WHERE currency = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            pstmt.setString(1, currency);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("rate");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas pobierania kursu waluty: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}

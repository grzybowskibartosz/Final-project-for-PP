package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class okienka {
    private JTextArea data_visualization;
    private JPanel mainPanel;
    private JButton download_button;
    private JButton search_button;
    private JButton add_button;
    private JTextField search_field;
    private JTextField add_field;
    private JButton clear_button;
    private JButton delete_button;
    private JTextField delete_field;
    private JButton refresh_button;
    private JComboBox<String> currencies_1;
    private JComboBox<String> currencies_2;
    private JTextField sum_to_exchange;
    private JTextField sum_exchanged;
    private JTextField rate_from_currency;
    private JTextField rate_to_currency;
    private JTabbedPane tabbedPane;
    private JPanel panel1;
    private JPanel panel2;
    private JButton exchange_button;

    private http_client httpClient;
    private data_controller dataController;
    private deserializer Deserializer;

    public okienka() {
        $$$setupUI$$$();

        // Ustawienie tytułów zakładek po wygenerowaniu GUI
        tabbedPane.setTitleAt(0, "Currency Data");
        tabbedPane.setTitleAt(1, "Exchange Rates");

        httpClient = new http_client();
        dataController = new data_controller();
        Deserializer = new deserializer();

        setupListeners();

        populateCurrencyComboBoxes();
    }

    private void setupListeners() {
        download_button.addActionListener(e -> downloadData());
        search_button.addActionListener(e -> searchData());
        add_button.addActionListener(e -> addData());
        clear_button.addActionListener(e -> clearData());
        delete_button.addActionListener(e -> deleteData());
        refresh_button.addActionListener(e -> refreshData());
        exchange_button.addActionListener(e -> exchangeCurrency());
        currencies_1.addActionListener(e -> updateRateField(rate_from_currency, (String) currencies_1.getSelectedItem()));
        currencies_2.addActionListener(e -> updateRateField(rate_to_currency, (String) currencies_2.getSelectedItem()));
    }

    // Method to download data from an external source
    // 1. Send an HTTP request to the external data source.
    // 2. Receive the data and parse the JSON response.
    // 3. Update the data controller with the new data.
    // 4. Update the UI to display the downloaded data.
    private void downloadData() {
        String apiUrl = "https://openexchangerates.org/api/latest.json?app_id=0f6fa859368245b9a7da40eab440282b";
        try {
            String jsonResponse = httpClient.getApiResponse(apiUrl);
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            dataController.insertCurrencyRates(jsonObject);

            String displayString = Deserializer.jsonToString(jsonObject);
            data_visualization.setText(displayString);

        } catch (Exception e) {
            e.printStackTrace();
            data_visualization.setText("Error downloading data: " + e.getMessage());
        }
    }

    // Method to search for specific data based on user input
    // 1. Get the search term from the search_field.
    // 2. Query the data controller with the search term.
    // 3. Retrieve the search results.
    // 4. Display the search results in the data_visualization text area.
    private void searchData() {
        String currency_code = search_field.getText().trim();
        if (currency_code.isEmpty()) {
            data_visualization.setText("Please enter a currency code to search.");
            return;
        }

        String result = dataController.searchCurrencyRate(currency_code);
        data_visualization.setText(result);
    }

    // Method to add new data based on user input
    // 1. Get the new data from the add_field.
    // 2. Send the new data to the data controller to be added.
    // 3. Update the data controller with the new data.
    // 4. Refresh the UI to show the added data.
    private void addData() {
        String newCurrencyData = add_field.getText();
        if (newCurrencyData.isEmpty()) {
            data_visualization.setText("Please enter currency data to add.");
            return;
        }

        String[] parts = newCurrencyData.split(":");
        if (parts.length != 2) {
            data_visualization.setText("Invalid format. Please use <currency>:<rate>.");
            return;
        }

        String currency = parts[0].trim();
        double rate;
        try {
            rate = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            data_visualization.setText("Invalid rate value. Please enter a valid number.");
            return;
        }

        if (dataController.addCurrencyRate(currency, rate)) {
            data_visualization.setText("Successfully added currency: " + currency + " with rate: " + rate);
        } else {
            data_visualization.setText("Error adding currency: " + currency);
        }
    }

    // Method to clear the data visualization area
    // 1. Clear the text in the data_visualization text area.
    private void clearData() {
        if (dataController.clearAllData()) {
            data_visualization.setText("Database cleared successfully.");
        } else {
            data_visualization.setText("Error clearing database.");
        }
    }

    // Method to delete data based on user input
    // 1. Get the identifier of the data to delete from the delete_field.
    // 2. Send the identifier to the data controller to delete the data.
    // 3. Update the data controller to remove the data.
    // 4. Refresh the UI to reflect the deletion.
    private void deleteData() {
        String deleteQuery = delete_field.getText();
        if (deleteQuery.isEmpty()) {
            data_visualization.setText("Please enter a currency code to delete.");
            return;
        } else {
            if (dataController.deleteCurrencyRate(deleteQuery)) {
                data_visualization.setText("Successfully deleted data: " + deleteQuery);
            } else {
                data_visualization.setText("Error deleting data: " + deleteQuery);
            }
        }
    }

    private void displayCurrencies() {
        List<String> currencies = dataController.getAllCurrencyRates();
        StringBuilder displayText = new StringBuilder();
        for (String currency : currencies) {
            displayText.append(currency).append("\n");
        }
        data_visualization.setText(displayText.toString());
    }

    // Method to exchange currency based on user input
    // 1. Get the source and target currencies from the combo boxes (currencies_1 and currencies_2).
    // 2. Get the amount to exchange from sum_to_exchange.
    // 3. Send an HTTP request to get the current exchange rate.
    // 4. Calculate the exchanged amount.
    // 5. Display the exchanged amount in sum_exchanged.
    // 6. Update rate_from_currency and rate_to_currency with the current rates.
    private void exchangeCurrency() {
        String fromCurrency = (String) currencies_1.getSelectedItem();
        String toCurrency = (String) currencies_2.getSelectedItem();
        String amountText = sum_to_exchange.getText();

        if (fromCurrency == null || toCurrency == null || amountText.isEmpty()) {
            sum_exchanged.setText("Please fill all fields.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            sum_exchanged.setText("Invalid amount.");
            return;
        }

        double fromRate = dataController.getCurrencyRate(fromCurrency);
        double toRate = dataController.getCurrencyRate(toCurrency);

        if (fromRate == 0 || toRate == 0) {
            sum_exchanged.setText("Invalid currency rates.");
            return;
        }

        double exchangedAmount = amount * toRate / fromRate;
        sum_exchanged.setText(String.valueOf(exchangedAmount));
    }


    // Additional methods
    private void refreshData() {
        displayCurrencies();
        populateCurrencyComboBoxes();
    }

    private void populateCurrencyComboBoxes() {
        List<String> currencies = dataController.getAllCurrencyRates();
        currencies_1.removeAllItems();
        currencies_2.removeAllItems();
        for (String currency : currencies) {
            String currencyCode = currency.split(":")[0].trim();
            currencies_1.addItem(currencyCode);
            currencies_2.addItem(currencyCode);
        }
    }

    private void updateRateField(JTextField rateField, String currency) {
        if (currency != null) {
            double rate = dataController.getCurrencyRate(currency);
            rateField.setText(String.valueOf(rate));
        } else {
            rateField.setText("");
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Currency Rates");
        frame.setContentPane(new okienka().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setName("Edycja bazy");
        panel1.setToolTipText("");
        tabbedPane.addTab("Untitled", panel1);
        download_button = new JButton();
        download_button.setText("Pobierz dane ");
        panel1.add(download_button, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(332, 34), null, 0, false));
        clear_button = new JButton();
        clear_button.setText("Wyczyść bazę");
        panel1.add(clear_button, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        refresh_button = new JButton();
        refresh_button.setText("Odśwież bazę");
        panel1.add(refresh_button, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        search_button = new JButton();
        search_button.setText("Wszukaj");
        panel1.add(search_button, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        add_button = new JButton();
        add_button.setText("Dodaj walutę");
        panel1.add(add_button, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        delete_button = new JButton();
        delete_button.setText("Usuń walutę");
        panel1.add(delete_button, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        delete_field = new JTextField();
        panel1.add(delete_field, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        add_field = new JTextField();
        panel1.add(add_field, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        search_field = new JTextField();
        panel1.add(search_field, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        data_visualization = new JTextArea();
        scrollPane1.setViewportView(data_visualization);
        panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(9, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setName("Kantor");
        tabbedPane.addTab("Untitled", panel2);
        sum_to_exchange = new JTextField();
        panel2.add(sum_to_exchange, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sum_exchanged = new JTextField();
        panel2.add(sum_exchanged, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        currencies_1 = new JComboBox();
        panel2.add(currencies_1, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        currencies_2 = new JComboBox();
        panel2.add(currencies_2, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exchange_button = new JButton();
        exchange_button.setText("Wymień walutę");
        panel2.add(exchange_button, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rate_from_currency = new JTextField();
        panel2.add(rate_from_currency, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        rate_to_currency = new JTextField();
        panel2.add(rate_to_currency, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Kurs żądanej waluty");
        panel2.add(label1, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Kurs waluty do wymiany");
        panel2.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Wartość do wymiany");
        panel2.add(label3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Wymieniona wartość");
        panel2.add(label4, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

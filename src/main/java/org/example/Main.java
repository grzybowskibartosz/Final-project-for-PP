package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {
    public static void main(String[] args) {
        String apiUrl = "https://openexchangerates.org/api/latest.json?app_id=0f6fa859368245b9a7da40eab440282b";  // Zastąp właściwym URL API
        http_client httpClient = new http_client();
        data_controller dataController = new data_controller();
        deserializer deserializer = new deserializer();

        try {
            dataController.testConnection();
            String jsonResponse = httpClient.getApiResponse(apiUrl);
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            System.out.println("Pobrany JSON: " + jsonObject.toString());
            dataController.insertCurrencyRates(jsonObject);


            // Przeciążenie do stringa i wyświetlenie w GUI
            String displayString = deserializer.jsonToString(jsonObject);
            System.out.println(displayString);

            // Możesz teraz użyć displayString do wyświetlenia w GUI
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
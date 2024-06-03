package org.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public class deserializer {
    public String jsonToString(JsonObject jsonObject) {
        StringBuilder result = new StringBuilder();
        JsonObject rates = jsonObject.getAsJsonObject("rates");

        for (Map.Entry<String, JsonElement> entry : rates.entrySet()) {
            String currency = entry.getKey();
            double rate = entry.getValue().getAsDouble();
            result.append("Currency: ").append(currency).append(", Rate: ").append(rate).append("\n");
        }

        return result.toString();
    }
}

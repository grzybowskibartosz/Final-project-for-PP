package org.example;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class http_client {
    public String getApiResponse(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStreamReader reader = new InputStreamReader(conn.getInputStream());
        StringBuilder response = new StringBuilder();
        int data = reader.read();
        while (data != -1) {
            response.append((char) data);
            data = reader.read();
        }
        reader.close();
        return response.toString();
    }
}

package org.cartman.api;

// Imports
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.cartman.Application;

public final class Urlscanio {
    /**
     * Sends url to be scanned in urlscan.io.
     * @param targetUrl The target url.
     * @return The response content.
     */
    public static String scanUrl(String targetUrl) {
        try {
            URL url = new URL("https://urlscan.io/api/v1/scan/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("API-Key", Application.getConfigParser().getString("urlscanio.apikey"));
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = ("{\"url\": \"" + targetUrl + "\", \"visibility\": \"public\"}").getBytes();
                os.write(input, 0, input.length);
            }

            if(conn.getResponseCode() == 400) {
                return "invalidkey";
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            conn.disconnect();

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            String message = json.get("message").getAsString();
            String uuid = json.get("uuid").getAsString();
            String result = json.get("result").getAsString();

            return """
               **%s**
               UUID: %s
               Result: %s
               """.formatted(message, uuid, result);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
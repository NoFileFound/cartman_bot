package org.cartman.api;

// Imports
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.cartman.Application;

public final class MobileLookup {
    /**
     * Gets information for the phone number.
     */
    public static String getPhoneNumberInfo(String phoneNumber) {
        try {
            URL url = new URL("https://apilayer.net/api/validate?access_key=" + Application.getConfigParser().getString("phone.apikey") + "&number=" + phoneNumber);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            con.disconnect();
            if(response.toString().contains("You have not supplied a valid API Access Key.")) {
                return "invalidkey";
            }

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            String number = json.has("international_format") ? json.get("international_format").getAsString() : "N/A";
            String country_name = json.has("country_name") ? json.get("country_name").getAsString() : "N/A";
            String country_code = json.has("country_code") ? json.get("country_code").getAsString() : "N/A";
            String location = json.has("location") ? json.get("location").getAsString() : "N/A";
            String carrier = json.has("carrier") ? json.get("carrier").getAsString() : "N/A";
            String line_type = json.has("line_type") ? json.get("line_type").getAsString() : "N/A";
            return String.format("Number: %s\nLocation: %s\nCountry: %s (%s)\nCarrier : %s\nLine type: %s", number, location, country_name, country_code, carrier, line_type);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
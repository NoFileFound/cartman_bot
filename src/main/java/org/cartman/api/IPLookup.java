package org.cartman.api;

// Imports
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.cartman.Application;

public final class IPLookup {
    /**
     * Gets information for the ip address.
     */
    public static String getIPAddressInfo(String ipAddress) {
        try {
            URL url = new URL("https://api.ipdata.co/" + ipAddress + "?api-key=" + Application.getConfigParser().getString("ipdata.apikey"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            con.disconnect();
            if (status != 200) {
                if (response.toString().contains("You have not provided a valid API Key.")) {
                    return "invalidkey";
                }

                if(response.toString().contains("is not a valid IPv4 or IPv6 address.")) {
                    return "invalidip";
                }

                return "reservedip";
            }

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            String ip = json.get("ip").getAsString();
            String city = json.get("city").getAsString();
            String region = json.get("region").getAsString();
            String country_name = json.get("country_name").getAsString();
            String country_code = json.get("country_code").getAsString();
            String asnNumber = json.getAsJsonObject("asn").get("asn").getAsString();
            String asnName = json.getAsJsonObject("asn").get("name").getAsString();
            String route = json.getAsJsonObject("asn").get("route").getAsString();
            String threatSummary = "N/A";
            if (json.has("threat") && json.get("threat").isJsonObject()) {
                JsonObject threat = json.getAsJsonObject("threat");
                boolean isTor = threat.has("is_tor") && threat.get("is_tor").getAsBoolean();
                boolean isIcloudRelay = threat.has("is_icloud_relay") && threat.get("is_icloud_relay").getAsBoolean();
                boolean isProxy = threat.has("is_proxy") && threat.get("is_proxy").getAsBoolean();
                boolean isDatacenter = threat.has("is_datacenter") && threat.get("is_datacenter").getAsBoolean();
                boolean isAnonymous = threat.has("is_anonymous") && threat.get("is_anonymous").getAsBoolean();
                boolean isKnownAttacker = threat.has("is_known_attacker") && threat.get("is_known_attacker").getAsBoolean();
                boolean isKnownAbuser = threat.has("is_known_abuser") && threat.get("is_known_abuser").getAsBoolean();
                threatSummary = String.format(
                        "TOR: %b\niCloud Relay: %b\nProxy: %b\nDatacenter: %b\nAnonymous: %b\nKnown Attacker: %b\nKnown Abuser: %b",
                        isTor, isIcloudRelay, isProxy, isDatacenter, isAnonymous, isKnownAttacker, isKnownAbuser
                );
            }

            return String.format("IP: %s\nHostname: %s (%s)\nCity: %s\nRegion: %s\nCountry: %s (%s)\nRoute : %s\n\nAdditional details: \n%s", ip, asnName, asnNumber, city, region, country_name, country_code, route, threatSummary);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
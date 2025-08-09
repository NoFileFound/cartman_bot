package org.cartman.api;

// Imports
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.cartman.Application;

public final class Playit {
    /**
     * Reports a playit.gg abuse endpoint.
     * @param portType The port type (UDP/TCP).
     * @param endpoint The endpoint.
     */
    public static void reportEndpoint(String portType, String endpoint) {
        try {
            URL url = new URL("https://api.playit.gg/abuse/report");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Api-Key " + Application.getConfigParser().getString("playit.apikey"));
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = String.format("""
                    {
                        "port_type": "%s",
                        "abuse_type": "MalwareC2C",
                        "endpoint": "%s",
                        "public_report_urls": []
                    }
                    """, portType, endpoint);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes();
                os.write(input, 0, input.length);
            }
            conn.disconnect();
        } catch (IOException _) {

        }
    }
}
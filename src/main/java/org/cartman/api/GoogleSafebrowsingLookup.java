package org.cartman.api;

// Imports
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.cartman.Application;

public final class GoogleSafebrowsingLookup {
    /**
     * Fetches information about the url from google safebrowsing api.
     * @param targetUrl The given url.
     */
    public static String getGoogleSafeBrowsingInfo(String targetUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + Application.getConfigParser().getString("googlesafebrowsing.apikey")).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String payload = """
                {
                  "client": {
                    "clientId": "Cartman",
                    "clientVersion": "0.2b"
                  },
                  "threatInfo": {
                    "threatTypes": [
                      "MALWARE",
                      "SOCIAL_ENGINEERING",
                      "UNWANTED_SOFTWARE",
                      "POTENTIALLY_HARMFUL_APPLICATION"
                    ],
                    "platformTypes": ["ANY_PLATFORM"],
                    "threatEntryTypes": ["URL"],
                    "threatEntries": [
                      { "url": "%s" }
                    ]
                  }
                }
                """.formatted(targetUrl);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes();
                os.write(input, 0, input.length);
            }

            if(conn.getResponseCode() == 400) {
                return "invalidkey";
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            conn.disconnect();
            JsonObject obj = JsonParser.parseString(response.toString()).getAsJsonObject();
            if (!obj.has("matches")) {
                return "notfound";
            }

            JsonArray matches = obj.getAsJsonArray("matches");
            StringBuilder sb = new StringBuilder();
            for (JsonElement element : matches) {
                JsonObject match = element.getAsJsonObject();
                String threatType = match.get("threatType").getAsString().toLowerCase();
                String platformType = match.get("platformType").getAsString().toLowerCase();
                String url = match.getAsJsonObject("threat").get("url").getAsString();
                String cache = match.get("cacheDuration").getAsString();
                threatType = Character.toUpperCase(threatType.charAt(0)) + threatType.substring(1);
                platformType = platformType.replace("_platform", "").replace("_", " ").trim();
                platformType = Character.toUpperCase(platformType.charAt(0)) + platformType.substring(1);
                if (cache.endsWith("s")) {
                    cache = cache.replace("s", " seconds");
                }

                sb.append("Threat Type -> ").append(threatType).append(",\n");
                sb.append("Platform Type -> ").append(platformType).append("\n");
                sb.append("URL -> ").append(url).append("\n");
                sb.append("Cache -> ").append(cache).append("\n\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Checks if the target url is found in google safebrowsing.
     * @param targetUrl The given url.
     * @return True if its found.
     */
    public static boolean isFoundInGoogleSafeBrowsing(String targetUrl) {
        return getGoogleSafeBrowsingInfo(targetUrl).contains("Threat");
    }

    /**
     * Reports an url to google safebrowsing.
     * @param threatType The threat type.
     * @param urlToReport The url to report.
     */
    public static void reportUrl(String threatType, String urlToReport) {
        try {
            URL url = new URL("https://safebrowsing.googleapis.com/v4/threatHits?key=" + Application.getConfigParser().getString("googlesafebrowsing.apikey"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String payload = """
            {
              "threat": {
                "threatType": "%s",
                "platformType": "ANY",
                "threatEntryType": "URL",
                "threat": {
                  "url": "%s"
                }
              },
              "client": {
                "clientId": "Cartman",
                "clientVersion": "0.2b"
              }
            }
            """.formatted(threatType, urlToReport);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes();
                os.write(input, 0, input.length);
            }
            conn.disconnect();
        } catch (Exception _) {

        }
    }
}
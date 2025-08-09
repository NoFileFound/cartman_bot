package org.cartman.api;

// Imports
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.cartman.database.DBManager;

public final class Proxy {
    /**
     * Reloads the proxies.
     * @return The length of new added proxies.
     */
    public static int reloadProxies() {
        DBManager.getProxies().clear();
        try {
            URL url = new URL("https://api.proxyscrape.com/v2/?request=getproxies&protocol=socks4&timeout=10000&country=all");
            URLConnection conn = url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        DBManager.getProxies().add(line.trim());
                    }
                }
            }

            return DBManager.getProxies().size();
        } catch (Exception _) {
            return -1;
        }
    }
}
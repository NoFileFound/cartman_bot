package org.cartman.api;

// Imports
import org.apache.commons.net.whois.WhoisClient;
import java.io.IOException;

public final class DomainLookup {
    private static final WhoisClient whois = new WhoisClient();

    /**
     * Get WHOIS info for the domain.
     */
    public static String getWhoisInfo(String domain) {
        try {
            whois.connect(WhoisClient.DEFAULT_HOST);
            String data = whois.query("=" + domain);
            whois.disconnect();
            if(data.contains("No match for")) {
                return "notfound";
            }

            return data.substring(0, data.lastIndexOf("URL of the ICANN Whois Inaccuracy Complaint Form: https://www.icann.org/wicf/"));
        } catch (IOException e) {
            return "unknown";
        }
    }
}
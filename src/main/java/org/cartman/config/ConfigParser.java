package org.cartman.config;

// Imports
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.cartman.Application;

public final class ConfigParser {
    private final Properties properties = new Properties();

    /**
     * Initializes the config file.
     */
    public ConfigParser() {
        try (InputStream input = new FileInputStream("config.properties")) {
            this.properties.load(input);
        } catch (IOException ex) {
            Application.getLogger().error("Failed to load config file: {}", ex.getMessage());
        }
    }

    /**
     * Gets the boolean value by given key.
     * @param key The given key.
     * @return Boolean
     */
    public boolean getBoolean(String key) {
        String val = this.properties.getProperty(key);
        return Boolean.parseBoolean(val);
    }

    /**
     * Gets the int value by given key.
     * @param key The given key.
     * @return Integer.
     */
    public long getLong(String key) {
        try {
            return Long.parseLong(this.properties.getProperty(key));
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }

    /**
     * Gets the string value by given key.
     * @param key The given key.
     * @return String.
     */
    public String getString(String key) {
        return this.properties.getProperty(key);
    }
}
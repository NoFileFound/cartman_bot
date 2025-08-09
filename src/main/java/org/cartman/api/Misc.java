package org.cartman.api;

// Imports
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.cartman.Application;
import org.cartman.database.DBManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public final class Misc {
    /**
     * Computes the Adler-32 hash.
     *
     * @param url The input URL.
     * @return The Adler-32 hash as a hexadecimal string.
     */
    public static String computeHash(String url) {
        byte[] bytes = url.getBytes();
        Checksum checksum = new Adler32();
        checksum.update(bytes, 0, bytes.length);
        long hashValue = checksum.getValue();
        return String.format("%08x", hashValue);
    }

    /**
     * Extracts the google tag from url context.
     *
     * @param context The url context.
     * @return The google tag. (UA-131285145-1 for example).
     */
    public static String extractAnyGoogleTag(String context) {
        Pattern anyPattern = Pattern.compile("(UA-\\d{4,10}-\\d{1,4})|(G-[A-Z0-9]{8,12})|(AW-\\d+)|(GT-\\w+)");
        Matcher matcher = anyPattern.matcher(context);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    /**
     * Gets the language of a page by specific words.
     *
     * @param context The content in a page.
     * @return The language which page is from.
     */
    public static String getLanguageByContext(String context) {
        if (context.contains("サポートに")) {
            return "Japanese";
        }

        if (context.contains("l'accès")) {
            return "French";
        }

        if (context.toLowerCase().contains("warnung") || context.contains("blockiert") || context.contains("technischen")) {
            return "German";
        }

        if (context.toLowerCase().contains("advertencia")) {
            return "Spanish";
        }

        return "English";
    }

    /**
     * Searches for a phone numbers in the context.
     * @param imageFile The image file location.
     * @return List of phone numbers if found.
     */
    public static List<String> findPhoneNumbers(String imageFile) throws TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage("eng");
        tesseract.setDatapath("C:\\Program Files (x86)\\Tesseract-OCR\\tessdata");
        Set<String> numbers = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("\\+?\\d[\\d\\s\\-()]{7,}\\d").matcher(tesseract.doOCR(new File(imageFile)));
        while (matcher.find()) {
            String raw = matcher.group();
            String normalized = raw.replaceAll("[\\s\\-()]", "");
            String digitsOnly = normalized.startsWith("+") ? "+" + normalized.substring(1).replaceAll("\\D", "") : normalized.replaceAll("\\D", "");
            int lengthCheck = digitsOnly.startsWith("+") ? digitsOnly.length() - 1 : digitsOnly.length();
            if (lengthCheck >= 10 && lengthCheck <= 15) {
                numbers.add((digitsOnly));
            }
        }

        return new ArrayList<>(numbers);
    }

    /**
     * Goes to the url, gets the required information and takes a screenshot.
     *
     * @param url The given url.
     */
    public static boolean reportUrl(String url, String reason) {
        Random random = new Random();
        var proxies = DBManager.getProxies();
        var urlHash = computeHash(url);
        while(!proxies.isEmpty()) {
            String proxy = proxies.get(random.nextInt(proxies.size()));
            String[] parts = proxy.split(":");

            try {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless=new");
                options.addArguments("--ignore-certificate-errors");
                options.addArguments("--proxy-server=socks4://" + parts[0] + ":" + parts[1]);
                options.addArguments("--window-size=1280,1010");
                WebDriver driver = new ChromeDriver(options);
                driver.get(url);

                String htmlContent = driver.getPageSource();
                File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.createDirectories(Path.of("data/" + urlHash));
                File target = new File("data/" + urlHash + "/screenshot.png");
                Files.copy(screenshotFile.toPath(), target.toPath());
                driver.quit();

                DBManager.getReportedUrls().add(urlHash);
                try (FileWriter writer = new FileWriter("data/" + urlHash + "/info.txt")) {
                    String googleTag = extractAnyGoogleTag(htmlContent);

                    writer.write("Link -> " + url + "\n");
                    if(reason.equals("tss")) {
                        writer.write("Type -> Tech Support Scam (Phishing)\n");
                        writer.write("Phone -> " + findPhoneNumbers("data/" + urlHash + "/screenshot.png") + "\n");
                        writer.write("Country -> " + getLanguageByContext(htmlContent) + "\n");
                    } else {
                        writer.write("Type -> Phishing\n");
                    }

                    writer.write("Hash -> " + urlHash + "\n");
                    writer.write("Google Safebrowsing -> " + GoogleSafebrowsingLookup.isFoundInGoogleSafeBrowsing(url) + "\n");
                    if(!googleTag.isEmpty())
                        writer.write("Google Tag -> " + extractAnyGoogleTag(htmlContent) + "\n");
                }
                return true;
            } catch (Exception e) {
                if(e.getMessage().contains("ERR_PROXY_CONNECTION_FAILED")) {
                    Application.getLogger().warn(String.format("[%s] Proxy not working, switching...", urlHash));
                    proxies.remove(proxy);
                    continue;
                }
                break;
            }
        }
        return false;
    }
}
package org.cartman.api;

// Imports
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.cartman.Application;

public final class SMTP {
    private static Session session;

    /**
     * Initializes the SMTP.
     */
    public static void initSMTP() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", Application.getConfigParser().getString("smtp.host"));
        props.put("mail.smtp.port", Application.getConfigParser().getLong("smtp.port"));

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Application.getConfigParser().getString("smtp.username"), Application.getConfigParser().getString("smtp.password"));
            }
        });
    }

    /**
     * Sends an email.
     * @param toEmail The destination.
     * @param subject The subject.
     * @param context The context.
     * @param isPhone Is for mobile number or url/domain.
     */
    public static void sendEmail(String toEmail, String subject, String context, boolean isPhone) throws MessagingException, IOException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(Application.getConfigParser().getString("smtp.username") + "@" + Application.getConfigParser().getString("smtp.host")));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(String.format(new String(Files.readAllBytes(Path.of("data/defaultmsg" + ((isPhone) ? "phone" : "url" + ".txt")))), context));
        Transport.send(message);
    }
}
package org.lpnu.chef_app.util;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.util.Properties;

@Plugin(name = "SafeSMTP", category = "Core", elementType = "appender", printObject = true)
public final class SafeSmtpAppender extends AbstractAppender {

    private static final Dotenv dotenv = Dotenv.load();

    protected SafeSmtpAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions, null);
    }

    @PluginFactory
    public static SafeSmtpAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        return new SafeSmtpAppender(name, filter, layout, ignoreExceptions);
    }

    @Override
    public void append(LogEvent event) {
        String username = dotenv.get("SMTP_USER");
        String password = dotenv.get("SMTP_PASS");
        String to = dotenv.get("SMTP_TO");

        if (username == null || password == null || to == null) return;

        String logMessage = new String(getLayout().toByteArray(event));

        new Thread(() -> {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "587");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject("Критична помилка в додатку Chef App!");
                message.setText(logMessage);

                Transport.send(message);
            } catch (MessagingException e) {
                System.err.println("[SafeSMTP] Не вдалося надіслати лист: " + e.getMessage());
            }
        }).start();
    }
}
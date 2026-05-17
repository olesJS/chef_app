package org.lpnu.chef_app;

import javafx.application.Application;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.URL;
import org.apache.logging.log4j.LogManager;

public class Launcher {
    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure().load();
            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue())
            );

            java.util.logging.Logger.getLogger("jakarta.mail").setLevel(java.util.logging.Level.SEVERE);

            URL log4jConfig = Launcher.class.getResource("/org/lpnu/chef_app/log4j2.xml");
            if (log4jConfig != null) {
                System.setProperty("log4j.configurationFile", log4jConfig.toExternalForm());
                System.out.println("Launcher: Конфігурацію Log4j 2 знайдено та застосовано.");
            }

            LogManager.getLogger(Launcher.class).info("Логування успішно ініціалізовано.");

        } catch (Exception e) {
            System.err.println("Launcher: Помилка ініціалізації: " + e.getMessage());
        }

        Application.launch(ChefApplication.class, args);
    }
}
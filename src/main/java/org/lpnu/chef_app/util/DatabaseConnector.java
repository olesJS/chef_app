package org.lpnu.chef_app.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TimeZone;

public class DatabaseConnector {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnector.class);
    private static final Dotenv dotenv = Dotenv.load();

    // Setting "Europe/Kyiv" timezone because of Postgres default settings
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));
        System.setProperty("user.timezone", "Europe/Kyiv");
    }

    private static final String DB_HOST = dotenv.get("POSTGRES_HOST", "localhost");
    private static final String DB_PORT = dotenv.get("POSTGRES_PORT", "5432");
    private static final String DB_URL = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, dotenv.get("POSTGRES_DB"));
    private static final String DB_USER = dotenv.get("POSTGRES_USER");
    private static final String DB_PASSWORD = dotenv.get("POSTGRES_PASSWORD");

    public static Connection getConnection() throws SQLException {
        try {
            String testUrl = System.getProperty("test.db.url");
            if (testUrl != null) {
                return DriverManager.getConnection(testUrl, "sa", "");
            }

            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            log.error("Критична помилка підключення до БД! Перевірте налаштування сервера або .env", e);
            throw e;
        }
    }

}
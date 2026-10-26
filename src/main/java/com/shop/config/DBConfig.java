package com.shop.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import io.github.cdimascio.dotenv.Dotenv;

public class DBConfig {

    private static final Properties properties = new Properties();
    private static final Dotenv dotenv = Dotenv.load();

    static {
        try (InputStream input = DBConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException(
                        "application.properties not found");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not load database configuration", e);
        }
    }

    public static Connection getConnection() throws SQLException{
        String url = properties.getProperty("db.url");
        String username = dotenv.get("DB_USERNAME");
        String password = dotenv.get("DB_PASSWORD");

        if (username == null || password == null) {
            throw new IllegalStateException(
                "DB_USERNAME and DB_PASSWORD must be configured"
            );
        }

        return DriverManager.getConnection(url, username, password);
    }

}

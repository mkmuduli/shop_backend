package com.shop.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConfig {

    private record DbCredentials(String username, String password, String url) {
    }

    private static final HikariDataSource DATA_SOURCE = createDataSource();

    public static DbCredentials getDBCredential() {
        String username = AppConfig.getEnv("DB_USERNAME");
        String password = AppConfig.getEnv("DB_PASSWORD");

        if (username == null || password == null) {
            throw new IllegalStateException(
                    "DB_USERNAME and DB_PASSWORD must be configured");
        }

        String url = AppConfig.getProperty("db.url");

        return new DbCredentials(username, password, url);
    }

    public static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();

        DbCredentials credentials = getDBCredential();

        config.setJdbcUrl(credentials.url());
        config.setDriverClassName("org.postgresql.Driver");
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setPoolName("shop-backend-pool");

        return new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static void shutdown() {
        DATA_SOURCE.close();
    }

    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }

    private DBConfig() {
    }
}

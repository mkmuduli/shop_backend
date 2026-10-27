package com.shop.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConfig {

    // Fail fast instead of hanging ~30s (Hikari default) when the pool is exhausted.
    private static final long CONNECTION_TIMEOUT_MS = 5_000;
    // Retire connections after 30 min so long-lived idle sockets are recycled.
    private static final long MAX_LIFETIME_MS = 1_800_000;
    // Log a stack trace if code holds a connection longer than this (our
    // queries are sub-millisecond, so holding 30s means something is wrong).
    private static final long LEAK_DETECTION_MS = 30_000;

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

        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setMaxLifetime(MAX_LIFETIME_MS);
        config.setLeakDetectionThreshold(LEAK_DETECTION_MS);

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

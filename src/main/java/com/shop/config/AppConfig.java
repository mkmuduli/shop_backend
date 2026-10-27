package com.shop.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {

    private static final Properties properties = new Properties();
    private static final String ENV;
    private static final Dotenv DOTENV;
    private static final Set<String> ALLOWED_ENVS = Set.of("dev", "test", "prod");

    static {
        ENV = resolveEnv();

        DOTENV = Dotenv.configure()
                .directory(".")
                .filename(".env." + ENV)
                .load();

        try (InputStream input = DBConfig.class
                .getClassLoader()
                .getResourceAsStream("application-" + ENV + ".properties")) {
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

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    private static String resolveEnv() {
        String env = System.getProperty("APP_ENV", System.getenv("APP_ENV"));

        if (env == null || env.isBlank()) {
            env = "dev";
        }

        if (!ALLOWED_ENVS.contains(env)) {
            throw new IllegalArgumentException(
                    "Invalid environment: " + env);
        }

        return env;
    }

    public static String getEnv(String key) {
        return DOTENV.get(key);
    }

    private AppConfig() {
        // Prevent object creation
    }

}

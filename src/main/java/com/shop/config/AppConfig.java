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
        ENV = System.getenv().getOrDefault("APP_ENV", "dev");

        if (ENV == null) {
            throw new IllegalArgumentException("Please provide the APP_ENV by command line");
        }

        if (!ALLOWED_ENVS.contains(ENV)) {
            throw new IllegalArgumentException(
                    "Invalid environment: " + ENV);
        }

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

    public static String getEnv(String key) {
        return DOTENV.get(key);
    }

    private AppConfig() {
        // Prevent object creation
    }

}

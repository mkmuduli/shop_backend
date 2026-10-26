package com.shop;

import org.flywaydb.core.Flyway;

import com.shop.config.DBConfig;

public final class DatabaseMigrator {

    private DatabaseMigrator() {
    }

    public static void migrate() {
        Flyway.configure()
                .dataSource(DBConfig.getDataSource())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }
}

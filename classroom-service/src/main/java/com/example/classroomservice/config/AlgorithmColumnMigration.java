package com.example.classroomservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
public class AlgorithmColumnMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AlgorithmColumnMigration.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public AlgorithmColumnMigration(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            if (productName == null || !productName.toLowerCase().contains("mysql")) {
                return;
            }
        }

        alterAlgorithmColumn("assignments", true);
        alterAlgorithmColumn("student_progress", false);
    }

    private void alterAlgorithmColumn(String tableName, boolean notNull) {
        String nullability = notNull ? "NOT NULL" : "NULL";
        try {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " MODIFY COLUMN algorithm VARCHAR(64) " + nullability);
            log.info("Ensured {}.algorithm uses VARCHAR(64)", tableName);
        } catch (Exception ex) {
            log.warn("Could not alter {}.algorithm to VARCHAR(64): {}", tableName, ex.getMessage());
        }
    }
}

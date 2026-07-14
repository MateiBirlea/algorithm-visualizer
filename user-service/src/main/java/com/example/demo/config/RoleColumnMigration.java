package com.example.demo.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RoleColumnMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public RoleColumnMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN role varchar(32) NOT NULL");
        } catch (DataAccessException ignored) {
            // Some embedded/test databases do not support the MySQL ALTER syntax.
        }
    }
}

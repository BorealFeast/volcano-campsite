package com.borealfeast.reservation.integration;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = {"com.borealfeast"})
public class TestConfiguration {

    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:13");

    @Bean
    public DataSource containerPostgres() {
        postgreSQLContainer.start();
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(postgreSQLContainer.getJdbcUrl());
        ds.setUser(postgreSQLContainer.getUsername());
        ds.setPassword(postgreSQLContainer.getPassword());
        ds.setDatabaseName(postgreSQLContainer.getDatabaseName());
        return ds;
    }
}

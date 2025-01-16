package com.dzieger.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@Profile({"dev", "prod"})
public class DatabaseConfig {

    private final Parameters params;

    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;

    @Autowired
    public DatabaseConfig(Parameters params) {
        this.params = params;
    }

    @PostConstruct
    public void init() {
        this.databaseUrl = params.getDatabaseUrl();
        this.databaseUsername = params.getDatabaseUsername();
        this.databasePassword = params.getDatabasePassword();
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://" + databaseUrl);
        dataSource.setUsername(databaseUsername);
        dataSource.setPassword(databasePassword);
        dataSource.setSchema("catan_auth_db");
        return dataSource;
    }

}

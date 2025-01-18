package com.dzieger.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TestConfig {

    @Bean
    @Qualifier("databaseUrl")
    public String databaseUrl() {
        return "jdbc:h2:mem:testdb";
    }

    @Bean
    @Qualifier("databaseUsername")
    public String databaseUsername() {
        return "sa";
    }

    @Bean
    @Qualifier("databasePassword")
    public String databasePassword() {
        return "";
    }

    @Bean
    @Qualifier("jwtSecret")
    public String jwtSecret() {
        return "thisisaverysecuresecretkeyforsigningjwt123";
    }

    @Bean
    @Qualifier("jwtExpiration")
    public String jwtExpiration() {
        return "3600000";
    }

    @Bean
    @Qualifier("jwtIssuer")
    public String jwtIssuer() {
        return "testIssuer";
    }
}

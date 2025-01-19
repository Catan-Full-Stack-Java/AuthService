package com.dzieger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestParametersConfig {

    @Bean
    public Parameters parameters() {
        return new Parameters(
                "jdbc:h2:mem:testdb",
                "sa",
                "",
                "thisisaverysecretcodethatshouldnotbeshared",
                "360000",
                "testIssuer"
        );
    }

}

package com.dzieger.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

@Profile({"dev", "prod"})
@Configuration
public class ParamStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(ParamStoreConfig.class);

    private final Environment environment;

    public ParamStoreConfig(Environment environment) {
        this.environment = environment;
    }

    private String getActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length > 0 ? activeProfiles[0] : "default";
    }

    @Bean
    public SsmClient ssmClient() {
        log.info("Initializing SsmClient");
        return SsmClient.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private String getParameterName(String parameterName) {
        return String.format("/catan/%s/%s", getActiveProfile(), parameterName);
    }

    @Bean
    public String databaseUrl(SsmClient ssmClient) {
        GetParameterRequest request = GetParameterRequest.builder()
                .name(getParameterName("database/url"))
                .withDecryption(true)
                .build();

        return ssmClient.getParameter(request).parameter().value();
    }

    @Bean
    public String databaseUsername(SsmClient ssmClient) {
        GetParameterRequest request = GetParameterRequest.builder()
                .name(getParameterName("database/username"))
                .withDecryption(true)
                .build();

        return ssmClient.getParameter(request).parameter().value();
    }

    @Bean
    public String databasePassword(SsmClient ssmClient) {
        GetParameterRequest request = GetParameterRequest.builder()
                .name(getParameterName("database/password"))
                .withDecryption(true)
                .build();

        return ssmClient.getParameter(request).parameter().value();
    }

    @Bean
    public String jwtSecret(SsmClient ssmClient) {
        GetParameterRequest request = GetParameterRequest.builder()
                .name(getParameterName("jwt/secret"))
                .withDecryption(true)
                .build();

        return ssmClient.getParameter(request).parameter().value();
    }

    @Bean
    public String jwtExpiration(SsmClient ssmClient) {
        GetParameterRequest request = GetParameterRequest.builder()
                .name(getParameterName("jwt/expiration"))
                .withDecryption(true)
                .build();

        return ssmClient.getParameter(request).parameter().value();
    }

    @Bean
    public String jwtIssuer(SsmClient ssmClient) {
        GetParameterRequest request = GetParameterRequest.builder()
                .name(getParameterName("jwt/issuer"))
                .withDecryption(true)
                .build();

        return ssmClient.getParameter(request).parameter().value();
    }

}

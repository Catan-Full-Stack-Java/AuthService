package com.dzieger.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
public class Parameters {

    private static Logger log = LoggerFactory.getLogger(Parameters.class);

    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;

    private String jwtSecret;
    private String jwtExpiration;
    private String jwtIssuer;

    @Autowired
    public Parameters(
            @Qualifier("databaseUrl") String databaseUrl,
            @Qualifier("databaseUsername") String databaseUsername,
            @Qualifier("databasePassword") String databasePassword,
            @Qualifier("jwtSecret") String jwtSecret,
            @Qualifier("jwtExpiration") String jwtExpiration,
            @Qualifier("jwtIssuer") String jwtIssuer) {
        log.info("Initializing Parameters");
        this.databaseUrl = databaseUrl;
        this.databaseUsername = databaseUsername;
        this.databasePassword = databasePassword;
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
        this.jwtIssuer = jwtIssuer;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public String getJwtExpiration() {
        return jwtExpiration;
    }

    public String getJwtIssuer() {
        return jwtIssuer;
    }
}

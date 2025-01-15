package com.dzieger.SecurityConfig;

import com.dzieger.config.Parameters;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.List;
import java.util.UUID;


@Component
public class JwtUtil {

    private long jwtExpiration;
    private String jwtSecret;
    private String jwtIssuer;
    private Key KEY;

    private final Parameters params;

    public JwtUtil(Parameters params) {
        this.params = params;
    }

    @PostConstruct
    public void init() {
        this.jwtExpiration = Long.parseLong(params.getJwtExpiration());
        this.jwtSecret = params.getJwtSecret();
        this.jwtIssuer = params.getJwtIssuer();
        this.KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private Claims extractAllClaims(String token) {
        return io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public boolean validate(String token) {
        Claims claims = extractAllClaims(token);
        if (claims.getIssuer().equals(jwtIssuer)) {
            return !claims.getExpiration().before(new java.util.Date());
        } else {
            return false;
        }
    }

    public List<?> extractAuthorities(String token) {
        return extractAllClaims(token).get("authorities", List.class);
    }

    public String generateToken(UUID userId, List<String> authorities) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("authorities", authorities)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new java.util.Date(System.currentTimeMillis()))
                .setExpiration(new java.util.Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(KEY)
                .compact();
    }
}


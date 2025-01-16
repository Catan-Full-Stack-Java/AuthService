package com.dzieger.SecurityConfig;

import com.dzieger.config.Parameters;
import com.dzieger.exceptions.jwt.InvalidIssuerException;
import com.dzieger.exceptions.jwt.JwtExpiredException;
import com.dzieger.exceptions.jwt.JwtValidationException;
import com.dzieger.models.Player;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
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

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .setAllowedClockSkewSeconds(1) // Add 1 second of clock skew
                    .build()
                    .parseClaimsJws(token);
            if (!jwtIssuer.equals(extractAllClaims(token).getIssuer())) {
                throw new InvalidIssuerException("Invalid JWT issuer");
            }
            return jwtIssuer.equals(extractAllClaims(token).getIssuer());
        } catch (ExpiredJwtException e) {
            throw new JwtExpiredException("JWT expired at: " + e.getClaims().getExpiration());
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("Malformed JWT" + e.getMessage());
        } catch (SignatureException e) {
            throw new JwtValidationException("Invalid JWT signature" + e.getMessage());
        } catch (JwtException e) {
            throw new JwtValidationException("Invalid JWT" + e.getMessage());
        }
    }


    public List<?> extractAuthorities(String token) {
        return extractAllClaims(token).get("authorities", List.class);
    }

    public String generateToken(Player player, List<String> authorities) {
        return Jwts.builder()
                .setSubject(player.getId().toString())
                .claim("username", player.getUsername())
                .claim("authorities", authorities)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new java.util.Date(System.currentTimeMillis()))
                .setExpiration(new java.util.Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(KEY)
                .compact();
    }
}


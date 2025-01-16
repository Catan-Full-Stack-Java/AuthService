package com.dzieger.SecurityConfig;

import com.dzieger.config.Parameters;

import com.dzieger.exceptions.jwt.InvalidIssuerException;
import com.dzieger.exceptions.jwt.JwtExpiredException;
import com.dzieger.models.Player;
import com.dzieger.models.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class JwtUtilTest {

    @Mock
    private Parameters params;

    @InjectMocks
    private JwtUtil jwtUtil;

    private Key secretKey;
    private final String jwtSecret = "thisisaverysecuresecretkeyforsigningjwt123";
    private final String jwtIssuer = "testIssuer";
    private final long jwtExpiration = 3600000;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock Parameters
        when(params.getJwtSecret()).thenReturn(jwtSecret);
        when(params.getJwtIssuer()).thenReturn(jwtIssuer);
        when(params.getJwtExpiration()).thenReturn(String.valueOf(jwtExpiration));

        // Initialize JwtUtil
        jwtUtil.init();
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Test
    void testGenerateToken() {
        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setUsername("test");
        player.setRole(Role.PLAYER);

        List<String> authorities = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(player, authorities);
        assertNotNull(token);

        Claims claims = extractClaims(token);
        assertEquals(player.getId().toString(), claims.getSubject());
        assertEquals(authorities, claims.get("authorities"));
        assertEquals(jwtIssuer, claims.getIssuer());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void testValidateValidToken() {
        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setUsername("test");
        player.setRole(Role.PLAYER);

        List<String> authorities = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(player, authorities);
        assertTrue(jwtUtil.validate(token));
    }

    @Test
    void testValidateExpiredToken() throws InterruptedException {
        // Set expiration to a very short time
        when(params.getJwtExpiration()).thenReturn("1"); // 1 ms expiration
        jwtUtil.init();

        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setUsername("test");
        player.setRole(Role.PLAYER);

        List<String> authorities = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(player, authorities);

        // Ensure the token has expired
        Thread.sleep(5);

        // Assert that the JwtExpiredException is thrown
        JwtExpiredException exception = assertThrows(
                JwtExpiredException.class,
                () -> jwtUtil.validate(token)
        );

        assertTrue(exception.getMessage().contains("JWT expired at"));
    }



    @Test
    void testValidateInvalidIssuer() {
        UUID userId = UUID.randomUUID();
        List<String> authorities = List.of("ROLE_USER");

        // Create a token with a wrong issuer
        String token = Jwts.builder()
                .setSubject(userId.toString())
                .claim("authorities", authorities)
                .setIssuer("wrongIssuer") // Deliberate mismatch
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey)
                .compact();

        // Assert that the InvalidIssuerException is thrown
        InvalidIssuerException exception = assertThrows(
                InvalidIssuerException.class,
                () -> jwtUtil.validate(token)
        );

        assertEquals("Invalid JWT issuer", exception.getMessage());
    }


    @Test
    void testExtractUserId() {
        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setUsername("test");
        player.setRole(Role.PLAYER);

        List<String> authorities = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(player, authorities);
        UUID extractedUserId = jwtUtil.extractUserId(token);

        assertEquals(player.getId(), extractedUserId);
    }

    @Test
    void testExtractAuthorities() {
        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setUsername("test");
        player.setRole(Role.PLAYER);

        List<String> authorities = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(player, authorities);
        List<?> extractedAuthorities = jwtUtil.extractAuthorities(token);

        assertEquals(authorities, extractedAuthorities);
    }

    @Test
    void testExtractUsername() {
        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setUsername("test");
        player.setRole(Role.PLAYER);

        List<String> authorities = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(player, authorities);
        String extractedUsername = jwtUtil.extractUsername(token); // `username` claim is not set in the example

        assertEquals(player.getUsername(), extractedUsername); // Expected `null` since `username` is not included in `generateToken`
    }

    // Utility method to extract claims for testing
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

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
import org.springframework.core.env.Environment;
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
    private Environment env;

    @Mock
    private Parameters params;

    @InjectMocks
    private JwtUtil jwtUtil;

    private Key secretKey;
    private final long jwtExpiration = 5000;
    private final String jwtSecret = "thisisaverysecretcodethatshouldnotbeshared";
    private final String jwtIssuer = "testIssuer";


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the Environment
        when(env.getActiveProfiles()).thenReturn(new String[]{"test"});

        // Mock Parameters (if needed)
        when(params.getJwtSecret()).thenReturn(jwtSecret);
        when(params.getJwtExpiration()).thenReturn(String.valueOf(jwtExpiration));
        when(params.getJwtIssuer()).thenReturn(jwtIssuer);

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

        String token = Jwts.builder()
                .setSubject(player.getId().toString())
                .claim("username", player.getUsername())
                .claim("authorities", authorities)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000)) // 10 seconds in the past
                .setExpiration(new Date(System.currentTimeMillis() - 5000)) // Expired 5 seconds ago
                .signWith(secretKey)
                .compact();

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

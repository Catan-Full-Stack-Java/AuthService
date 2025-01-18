package com.dzieger.controllers;

import com.dzieger.SecurityConfig.JwtUtil;
import com.dzieger.models.Player;
import com.dzieger.models.enums.Role;
import com.dzieger.repositories.PlayerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeAll
    public void setUp() {
        playerRepository.deleteAll();
    }

    @BeforeEach
    void cleanUp() {
        playerRepository.deleteAll();
    }

    @Test
    void testRegister_createsNewPlayerSuccessfully() throws Exception {
        String requestBody = """
                {
                    "username": "newplayer",
                    "email": "newplayer@email.com",
                    "password": "Valid@123",
                    "firstName": "Player"
                }
                """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());

        Optional<Player> player = playerRepository.findByUsername("newplayer");
        assertTrue(player.isPresent());
        assertEquals("newplayer", player.get().getUsername());
        assertEquals("newplayer@email.com", player.get().getEmail());
        assertEquals("Player", player.get().getFirstName());
        assertEquals(player.get().getRole(), Role.PLAYER.toString());
        assertTrue(new BCryptPasswordEncoder().matches("Valid@123", player.get().getPassword()));
    }

    // Test Register endpoint with duplicate username
    @Test
    void testRegister_duplicateUsername() throws Exception {
        Player player = new Player();
        player.setUsername("test");
        player.setEmail("testuser@email.com");
        player.setPassword(new BCryptPasswordEncoder().encode("Valid@123"));
        player.setFirstName("John");
        player.setRole(Role.PLAYER);
        playerRepository.save(player);

        String requestBody = """
                {
                    "username": "test",
                    "email": "test@email.com",
                    "password": "Valid@123",
                    "firstName": "John"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    // Test Register endpoint with duplicate email
    @Test
    void testRegister_duplicateEmail() throws Exception {
        Player player = new Player();
        player.setUsername("test");
        player.setEmail("duplicate@email.com");
        player.setPassword(new BCryptPasswordEncoder().encode("Valid@123"));
        player.setFirstName("John");
        player.setRole(Role.PLAYER);
        playerRepository.save(player);

        String requestBody = """
                {
                    "username": "testuser",
                    "email": "duplicate@email.com",
                    "password": "Valid@123",
                    "firstName": "John"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    // Test Register endpoint with no username
    @Test
    void testRegister_noUsername() throws Exception {
        String requestBody = """
                {
                    "email": "test@email.com",
                    "password": "Valid@123",
                    "firstName": "John"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Test Register endpoint with no email
    @Test
    void testRegister_noEmail() throws Exception {
        String requestBody = """
                {
                    "username": "test",
                    "password": "Valid@123",
                    "firstName": "John"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Test Register endpoint with no password
    @Test
    void testRegister_noPassword() throws Exception {
        String requestBody = """
                {
                    "username": "test",
                    "email": "test@email.com",
                    "firstName": "John"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Test Register endpoint with no first name
    @Test
    void testRegister_noFirstName() throws Exception {
        String requestBody = """
                {
                    "username": "test",
                    "email": "test@email.com",
                    "password": "Valid@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Test Register endpoint with invalid email
    @Test
    void testRegister_invalidEmail() throws Exception {
        String requestBody = """
                {
                    "username": "test",
                    "email": "invalid",
                    "password": "Valid@123",
                    "firstName": "John"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Test Register endpoint with invalid password
    @Test
    void testRegister_invalidPassword() throws Exception {
        String requestBody = """
                {
                    "username": "test",
                    "email": "test@email.com",
                    "password": "invalid",
                    "firstName": "John"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Test login endpoint with valid credentials
    @Test
    void testLogin_shouldReturnToken_whenCredentialsAreValid() throws Exception {
        Player player = new Player();
        player.setUsername("test");
        player.setEmail("login@email.com");
        player.setPassword(new BCryptPasswordEncoder().encode("Valid@123"));
        player.setFirstName("John");
        player.setRole(Role.PLAYER);
        playerRepository.save(player);

        String requestBody = """
                {
                    "username": "test",
                    "password": "Valid@123"
                }
                """;

        ResultActions result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        String responseContent = result.andReturn().getResponse().getContentAsString();
        assertTrue(responseContent.contains("token"));

        ObjectMapper objectMapper = new ObjectMapper();
        String token = objectMapper.readTree(responseContent).get("token").asText();

        assertTrue(jwtUtil.validate(token));
        assertEquals(player.getUsername(), jwtUtil.extractUsername(token));
    }

    // Test login endpoint with invalid username
    @Test
    void testLogin_shouldReturnStatusBadRequest_whenUsernameIsInvalid() throws Exception {
        String requestBody = """
                {
                    "username": "invalid",
                    "password": "Valid@123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Test login endpoint with invalid password
    @Test
    void testLogin_shouldReturnStatusUnauthorized_whenPasswordIsInvalid() throws Exception {
        Player player = new Player();
        player.setUsername("test");
        player.setEmail("invalidpassword@email.com");
        player.setPassword(new BCryptPasswordEncoder().encode("Valid@123"));
        player.setFirstName("John");
        player.setRole(Role.PLAYER);
        playerRepository.save(player);

        String requestBody = """
                {
                    "username": "test",
                    "password": "invalid"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    // Test login endpoint with no username
    @Test
    void testLogin_shouldReturnStatusBadRequest_whenUsernameIsMissing() throws Exception {
        String requestBody = """
                {
                    "password": "Valid@123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Test login endpoint with no password
    @Test
    void testLogin_shouldReturnStatusBadRequest_whenPasswordIsMissing() throws Exception {
        String requestBody = """
                {
                    "username": "test"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }


}

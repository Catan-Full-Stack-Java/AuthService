package com.dzieger.services;

import com.dzieger.SecurityConfig.JwtUtil;
import com.dzieger.dtos.LoginDTO;
import com.dzieger.dtos.OutgoingAuthenticatedPlayerDTO;
import com.dzieger.dtos.OutgoingPlayerDTO;
import com.dzieger.dtos.RegisterDTO;
import com.dzieger.exceptions.DuplicateEmailException;
import com.dzieger.exceptions.DuplicateUsernameException;
import com.dzieger.models.Player;
import com.dzieger.models.enums.Role;
import com.dzieger.repositories.PlayerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PlayerProfileService playerProfileService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private PlayerService playerService;

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @Mock
    private static PasswordEncoder passwordEncoder;

    @BeforeAll
    static void setUpAll() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    @BeforeEach
    void setUp() {
        validator = validatorFactory.getValidator();
    }

    @Test
    void testRegister_returnsOutgoingPlayerDTO_whenRegisterDTOIsValid() {
        // Arrange
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("johndoe");
        registerDTO.setEmail("john.doe@example.com");
        registerDTO.setPassword("ValidPassword123!");

        Player mockPlayer = new Player();
        mockPlayer.setId(UUID.randomUUID());
        mockPlayer.setFirstName("John");
        mockPlayer.setUsername("johndoe");
        mockPlayer.setEmail("john.doe@example.com");
        mockPlayer.setPassword("EncodedPassword123!");
        mockPlayer.setRole(Role.PLAYER);

        // Mock repository behavior
        when(playerRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(mockPlayer);

        // Act
        OutgoingPlayerDTO result = playerService.register(registerDTO);

        // Assert
        assertEquals("John", result.getFirstName());
        assertEquals("johndoe", result.getUsername());
        assertEquals(Role.PLAYER.name(), result.getRole());
        verify(playerRepository).save(any(Player.class));
    }




    @Test
    void testRegister_encodesPassword_whenRegisterDTOIsValid() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("jdoe");
        registerDTO.setEmail("jdoe@test.com");
        registerDTO.setPassword("password");

        // Create a mock Player object to return from save
        Player savedPlayer = new Player();
        savedPlayer.setId(UUID.randomUUID());
        savedPlayer.setFirstName("John");
        savedPlayer.setUsername("jdoe");
        savedPlayer.setEmail("jdoe@test.com");
        savedPlayer.setPassword("encodedPassword"); // Simulate encoded password
        savedPlayer.setRole(Role.PLAYER);

        // Mock repository behavior
        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("jdoe@test.com")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        // Mock password encoding
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");

        // Act
        playerService.register(registerDTO);

        // Assert
        verify(passwordEncoder, times(1)).encode(registerDTO.getPassword());
        verify(playerRepository, times(1)).save(argThat(player ->
                player.getPassword().equals("encodedPassword") &&
                        player.getUsername().equals("jdoe") &&
                        player.getEmail().equals("jdoe@test.com")
        ));
    }



    @Test
    void testRegister_throwsDuplicateUsernameException_whenUsernameAlreadyExists() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("Jane");
        registerDTO.setUsername("jdoe2");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("password");

        Player existingPlayer = new Player();
        existingPlayer.setUsername("jdoe2");

        when(playerRepository.findByUsername("jdoe2")).thenReturn(Optional.of(existingPlayer));

        DuplicateUsernameException exception = assertThrows(
                DuplicateUsernameException.class,
                () -> playerService.register(registerDTO)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(playerRepository, times(1)).findByUsername("jdoe2");
    }

    @Test
    void testRegister_throwsDuplicateEmailException_whenEmailAlreadyExists() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("Jane");
        registerDTO.setUsername("test");
        registerDTO.setEmail("email@test.com");
        registerDTO.setPassword("password");

        Player existingPlayer = new Player();
        existingPlayer.setEmail("email@test.com");

        when(playerRepository.findByEmail("email@test.com")).thenReturn(Optional.of(existingPlayer));

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> playerService.register(registerDTO)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(playerRepository, times(1)).findByEmail("email@test.com");
    }

    @Test
    void testRegister_assignsDefaultRoleToNewPlayer_whenRegisterDTOIsValid() {
        // Arrange
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("jdoe");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("password");

        // Create a mock Player object to return from save
        Player savedPlayer = new Player();
        savedPlayer.setId(UUID.randomUUID());
        savedPlayer.setFirstName("John");
        savedPlayer.setUsername("jdoe");
        savedPlayer.setEmail("test@email.com");
        savedPlayer.setPassword("encodedPassword"); // Simulate encoded password
        savedPlayer.setRole(Role.PLAYER);

        // Mock repository behavior
        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        // Act
        OutgoingPlayerDTO result = playerService.register(registerDTO);

        // Assert
        assertNotNull(result);
        assertEquals(Role.PLAYER.toString(), result.getRole());
        verify(playerRepository, times(1)).save(any(Player.class));
    }



    @Test
    void testRegister_throwsException_whenRepositorySaveFails() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("jdoe");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("password");

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenThrow(new RuntimeException("Failed to save player"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> playerService.register(registerDTO)
        );

        assertEquals("Failed to save player", exception.getMessage());
    }

    @Test
    void testRegister_doesNotStorePlainTextPasswordInDatabase() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("jdoe");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("password");

        // Create a mock Player object to return from save
        Player savedPlayer = new Player();
        savedPlayer.setId(UUID.randomUUID());
        savedPlayer.setFirstName("John");
        savedPlayer.setUsername("jdoe");
        savedPlayer.setEmail("test@email.com");
        savedPlayer.setPassword("encodedPassword"); // Simulate encoded password
        savedPlayer.setRole(Role.PLAYER);

        // Mock repository behavior
        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        // Mock password encoding
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");


        // Act
        playerService.register(registerDTO);

        // Assert
        verify(playerRepository, times(1)).save(argThat(player ->
                player.getPassword().equals("encodedPassword") &&
                        !player.getPassword().equals("password") // Ensures plaintext password isn't stored
        ));
        verify(passwordEncoder, times(1)).encode("password");
    }



    @Test
    void testRegister_storesEmailInLowerCase_whenRegisterDTOIsValid() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("jdoe");
        registerDTO.setEmail("TestCase@EmaiL.com");
        registerDTO.setPassword("password");

        // Create a mock Player object to return from save
        Player savedPlayer = new Player();
        savedPlayer.setId(UUID.randomUUID());
        savedPlayer.setFirstName("John");
        savedPlayer.setUsername("jdoe");
        savedPlayer.setEmail("testcase@email.com"); // Email in lowercase
        savedPlayer.setPassword("encodedPassword"); // Simulate encoded password
        savedPlayer.setRole(Role.PLAYER);

        // Mock repository behavior
        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("testcase@email.com")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        // Act
        playerService.register(registerDTO);

        // Assert
        verify(playerRepository, times(1)).save(argThat(player -> player.getEmail().equals("testcase@email.com")));
    }



    @Test
    void testRegister_storesUsernameInLowerCase_whenRegisterDTOIsValid() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("TestUser");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("password");

        // Create a mock Player object to return from save
        Player savedPlayer = new Player();
        savedPlayer.setId(UUID.randomUUID());
        savedPlayer.setFirstName("John");
        savedPlayer.setUsername("testuser"); // Username in lowercase
        savedPlayer.setEmail("test@email.com");
        savedPlayer.setPassword("encodedPassword"); // Simulate encoded password
        savedPlayer.setRole(Role.PLAYER);

        // Mock repository behavior
        when(playerRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        // Act
        playerService.register(registerDTO);

        // Assert
        verify(playerRepository, times(1)).save(argThat(player -> player.getUsername().equals("testuser")));
    }


    @Test
    void testRegister_handlesLargeInputValues() {
        // Arrange: Prepare test data
        String longUsername = "a".repeat(21);
        String longEmail = "a".repeat(256) + "@email.com";

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername(longUsername);
        registerDTO.setEmail(longEmail);
        registerDTO.setPassword("password");

        Player savedPlayer = new Player();
        savedPlayer.setId(UUID.randomUUID());
        savedPlayer.setUsername(longUsername);
        savedPlayer.setEmail(longEmail);
        savedPlayer.setRole(Role.PLAYER);

        // Mock repository behavior
        when(playerRepository.findByUsername(longUsername)).thenReturn(Optional.empty());
        when(playerRepository.findByEmail(longEmail)).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        // Act & Assert: Verify no exceptions are thrown
        assertDoesNotThrow(() -> playerService.register(registerDTO));

        // Verify repository and service interactions
        verify(playerRepository, times(1)).findByUsername(longUsername);
        verify(playerRepository, times(1)).findByEmail(longEmail);
        verify(playerRepository, times(1)).save(any(Player.class));
    }






    // Login Tests

    @Test
    void testLogin_whenCredentialsAreValid() throws JsonProcessingException {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("jdoe");
        loginDTO.setPassword("password");

        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setUsername("jdoe");
        player.setPassword("encodedPassword");
        player.setFirstName("John");
        player.setRole(Role.PLAYER);

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(any(Player.class), any(List.class))).thenReturn("mockedJwtToken");

        OutgoingAuthenticatedPlayerDTO result = playerService.login(loginDTO);

        assertNotNull(result);
        assertEquals("jdoe", result.getUsername());
        assertEquals("mockedJwtToken", result.getToken());
        assertEquals("John", result.getFirstName());
        assertEquals(Role.PLAYER.toString(), result.getRole());
    }

    @Test
    void testLogin_throwsUsernameNotFoundException_whenPlayerNotFound() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("jdoe");
        loginDTO.setPassword("password");

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> playerService.login(loginDTO)
        );

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    void testLogin_throwsBadCredentialsException_whenPasswordIsIncorrect() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("jdoe");
        loginDTO.setPassword("password");

        Player player = new Player();
        player.setUsername("jdoe");
        player.setPassword("encodedPassword");

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(false);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> playerService.login(loginDTO)
        );

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void testLogin_isCaseInsensitive_forUsername() throws JsonProcessingException {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("JDOE");
        loginDTO.setPassword("password");

        Player player = new Player();
        player.setUsername("jdoe");
        player.setPassword("encodedPassword");
        player.setFirstName("John");
        player.setRole(Role.PLAYER);
        player.setId(UUID.randomUUID());
        player.setEmail("email@email.com");

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        OutgoingAuthenticatedPlayerDTO result = playerService.login(loginDTO);

        assertNotNull(result);
        assertEquals("jdoe", result.getUsername());
    }
}
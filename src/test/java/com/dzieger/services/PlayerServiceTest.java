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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

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
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("jdoe");
        registerDTO.setEmail("jdoe@test.com");
        registerDTO.setPassword("password");

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("jdoe@test.com")).thenReturn(Optional.empty());

        OutgoingPlayerDTO result = playerService.register(registerDTO);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("jdoe", result.getUsername());
        assertEquals(Role.PLAYER.toString(), result.getRole());
    }

    @Test
    void testRegister_encodesPassword_whenRegisterDTOIsValid() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("jdoe");
        registerDTO.setEmail("jdoe@test.com");
        registerDTO.setPassword("password");

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("jdoe@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");

        playerService.register(registerDTO);

        verify(passwordEncoder, times(1)).encode(registerDTO.getPassword());
    }

    @Test
    void testRegister_throwsDuplicateUsernameException_whenUsernameAlreadyExists() {
        // Arrange
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("Jane");
        registerDTO.setUsername("jdoe2");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("password");

        Player existingPlayer = new Player();
        existingPlayer.setUsername("jdoe2");

        // Mock the repository behavior
        when(playerRepository.findByUsername("jdoe2")).thenReturn(Optional.of(existingPlayer));

        // Act & Assert
        DuplicateUsernameException exception = assertThrows(
                DuplicateUsernameException.class,
                () -> playerService.register(registerDTO)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(playerRepository, times(1)).findByUsername("jdoe2");
    }

    @Test
    void testRegister_throwsDuplicateEmailException_whenEmailAlreadyExists() {
        // Arrange
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
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("jdoe");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("password");

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());

        OutgoingPlayerDTO result = playerService.register(registerDTO);

        assertEquals(Role.PLAYER.toString(), result.getRole());
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

        RuntimeException excetpion = assertThrows(
                RuntimeException.class,
                () -> playerService.register(registerDTO)
        );

        assertEquals("Failed to save player", excetpion.getMessage());
    }

    @Test
    void testRegister_doesNotStorePlainTextPasswordInDatabase() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("jdoe");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("password");

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        Player savedPlayer = new Player();
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        playerService.register(registerDTO);

        verify(playerRepository, times(1)).save(argThat(player -> player.getPassword().equals("encodedPassword")));
    }

    @Test
    void testRegister_storesEmailInLowerCase_whenRegisterDTOIsValid() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("jdoe");
        registerDTO.setEmail("TestCase@EmaiL.com");
        registerDTO.setPassword("password");

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("testcase@email.com")).thenReturn(Optional.empty());

        playerService.register(registerDTO);

        verify(playerRepository, times(1)).save(argThat(player -> player.getEmail().equals("testcase@email.com")));
    }

    @Test
    void testRegister_storesUsernameInLowerCase_whenRegisterDTOIsValid() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername("TestUser");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("password");

        when(playerRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());

        playerService.register(registerDTO);

        verify(playerRepository, times(1)).save(argThat(player -> player.getUsername().equals("testuser")));
    }

    @Test
    void testRegister_handlesLargeInputValues() {
        String longUsername = "a".repeat(21);
        String longEmail = "a".repeat(256) + "@email.com";

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setUsername(longUsername);
        registerDTO.setEmail(longEmail);
        registerDTO.setPassword("password");

        when(playerRepository.findByUsername(longUsername)).thenReturn(Optional.empty());
        when(playerRepository.findByEmail(longEmail)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> playerService.register(registerDTO));
    }

    // Login Tests

    @Test
    void testLogin_whenCredentialsAreValid() {
        // Arrange
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
        when(jwtUtil.generateToken(player, List.of(Role.PLAYER.toString(), player.getUsername()))).thenReturn("mockedJwtToken");

        // Act
        OutgoingAuthenticatedPlayerDTO result = playerService.login(loginDTO);

        // Assert
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
    void testLogin_isCaseInsensitive_forUsername() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("JDOE");
        loginDTO.setPassword("password");

        Player player = new Player();
        player.setUsername("jdoe");
        player.setPassword("encodedPassword");

        when(playerRepository.findByUsername("jdoe")).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        OutgoingAuthenticatedPlayerDTO result = playerService.login(loginDTO);

        assertNotNull(result);
        assertEquals("jdoe", result.getUsername());
    }

}

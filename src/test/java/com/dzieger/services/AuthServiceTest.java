package com.dzieger.services;

import com.dzieger.SecurityConfig.JwtUtil;
import com.dzieger.models.Player;
import com.dzieger.models.enums.Role;
import com.dzieger.repositories.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AuthServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        mockPlayer = new Player();
        mockPlayer.setId(UUID.randomUUID());
        mockPlayer.setUsername("test");
        mockPlayer.setRole(Role.PLAYER);
        mockPlayer.setPassword("Valid@123");
        mockPlayer.setFirstName("John");
        mockPlayer.setEmail("test@email.com");
    }

    @Test
    void testLoadUserByUsername_whenUsernameExists_shouldReturnUserDetails() {
        when(playerRepository.findByUsername("test")).thenReturn(Optional.of(mockPlayer));

        var userDetails = authService.loadUserByUsername("test");

        assertNotNull(userDetails);
        assertEquals(mockPlayer.getUsername(), userDetails.getUsername());
        assertEquals(mockPlayer.getPassword(), userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    void testLoadUserByUsername_whenUsernameDoesNotExist_shouldThrowUsernameNotFoundException() {
        when(playerRepository.findByUsername("test")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.loadUserByUsername("test"));
    }



}

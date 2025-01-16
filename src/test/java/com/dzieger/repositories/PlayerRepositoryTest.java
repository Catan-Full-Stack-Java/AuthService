package com.dzieger.repositories;

import com.dzieger.config.TestDatabaseConfig;
import com.dzieger.models.Player;
import com.dzieger.models.enums.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
public class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @BeforeAll
    static void logActiveProfile(@Value("${spring.profiles.active}") String activeProfile) {
        System.out.println("Active Profile: " + activeProfile);
    }


    @BeforeEach
    void cleanUp() {
        playerRepository.deleteAll();
    }

    @Autowired
    private ApplicationContext context;

    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        mockPlayer = new Player();
        mockPlayer.setUsername("test");
        mockPlayer.setPassword("Valid@123");
        mockPlayer.setFirstName("John");
        mockPlayer.setEmail("test@email.com");
        mockPlayer.setRole(Role.PLAYER);
    }

    @Test
    void testSaveAndFindById() {
        playerRepository.save(mockPlayer);
        Optional<Player> player = playerRepository.findById(mockPlayer.getId());

        assertTrue(player.isPresent());
        assertEquals(mockPlayer.getUsername(), player.get().getUsername());
    }

    @Test
    void testFindByUsername_whenUserExists() {
        playerRepository.save(mockPlayer);
        Optional<Player> player = playerRepository.findByUsername(mockPlayer.getUsername());

        assertTrue(player.isPresent());
        assertEquals(mockPlayer.getUsername(), player.get().getUsername());
    }

    @Test
    void testFindByUsername_whenUserDoesNotExist() {
        Optional<Player> player = playerRepository.findByUsername("nonexistent");

        assertTrue(player.isEmpty());
    }

    @Test
    void testFindByEmail_whenUserExists() {
        playerRepository.save(mockPlayer);
        Optional<Player> player = playerRepository.findByEmail(mockPlayer.getEmail());

        assertTrue(player.isPresent());
        assertEquals(mockPlayer.getEmail(), player.get().getEmail());
    }

    @Test
    void testFindByEmail_whenUserDoesNotExist() {
        Optional<Player> player = playerRepository.findByEmail("nonexistent");

        assertTrue(player.isEmpty());
    }

    @Test
    void testUniqueConstraints_onUsernameAndEmail() {
        playerRepository.saveAndFlush(mockPlayer);
        System.out.println("Player saved: " + mockPlayer);

        Player player = new Player();
        player.setUsername(mockPlayer.getUsername());
        player.setPassword("Valid@123");
        player.setFirstName("John");
        player.setEmail("different@email.com");
        player.setRole(Role.PLAYER);

        assertThrows(Exception.class, () -> playerRepository.saveAndFlush(player));

        Player duplicateEmail = new Player();
        duplicateEmail.setUsername("different");
        duplicateEmail.setPassword("Valid@123");
        duplicateEmail.setFirstName("John");
        duplicateEmail.setEmail(mockPlayer.getEmail());
        duplicateEmail.setRole(Role.PLAYER);

        assertThrows(Exception.class, () -> playerRepository.saveAndFlush(duplicateEmail));
    }

}

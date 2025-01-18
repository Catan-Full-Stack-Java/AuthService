package com.dzieger.services;

import com.dzieger.SecurityConfig.JwtUtil;
import com.dzieger.config.Parameters;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);


    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private KafkaTemplate<String, String> kafkaTemplate;

    public PlayerService(PlayerRepository playerRepository,
                         PasswordEncoder passwordEncoder,
                         JwtUtil jwtUtil,
                         KafkaTemplate<String, String> kafkaTemplate) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.kafkaTemplate = kafkaTemplate;

    }


    public OutgoingPlayerDTO register(RegisterDTO registerDTO) {
        log.info("Registering new player: {}", registerDTO.getUsername());

        checkDuplicateUsername(registerDTO.getUsername().toLowerCase());
        checkDuplicateEmail(registerDTO.getEmail().toLowerCase());

        // Create new player and persist to DB
        Player player = new Player();
        player.setFirstName(toTitleCase(registerDTO.getFirstName()));
        player.setUsername(registerDTO.getUsername().toLowerCase());
        player.setEmail(registerDTO.getEmail().toLowerCase());
        player.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        player.setRole(Role.PLAYER);
        playerRepository.save(player);

        // Send to Player Service microservice to create player profile

        OutgoingPlayerDTO outgoingPlayerDTO = new OutgoingPlayerDTO();
        outgoingPlayerDTO.setFirstName(player.getFirstName());
        outgoingPlayerDTO.setUsername(player.getUsername());
        outgoingPlayerDTO.setRole(player.getRole());

        return outgoingPlayerDTO;
    }

    private void checkDuplicateUsername(String username) {
        log.info("Checking for duplicate username: {}", username);

        if (playerRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException("Username already exists");
        }
    }

    private void checkDuplicateEmail(String email) {
        log.info("Checking for duplicate email");

        if (playerRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException("Email already exists");
        }
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public OutgoingAuthenticatedPlayerDTO login(LoginDTO loginDTO) throws JsonProcessingException {
        log.info("Logging in player: {}", loginDTO.getUsername());

        if (playerRepository.findByUsername(loginDTO.getUsername().toLowerCase()).isPresent()) {
            Player player = playerRepository.findByUsername(loginDTO.getUsername().toLowerCase()).orElseThrow(() ->
                    new UsernameNotFoundException("Player not found"));
            if (passwordEncoder.matches(loginDTO.getPassword(), player.getPassword())) {
                List<String> authorities = new ArrayList<>();
                authorities.add(player.getRole());

                OutgoingAuthenticatedPlayerDTO outgoingAuthenticatedPlayerDTO = new OutgoingAuthenticatedPlayerDTO();
                outgoingAuthenticatedPlayerDTO.setFirstName(player.getFirstName());
                outgoingAuthenticatedPlayerDTO.setUsername(player.getUsername());
                outgoingAuthenticatedPlayerDTO.setRole(player.getRole());
                outgoingAuthenticatedPlayerDTO.setToken(jwtUtil.generateToken(player, authorities));

                Map<Player, String> value = Map.of(player, player.getRole());
                String jsonValue = new ObjectMapper().writeValueAsString(value);

                kafkaTemplate.send("profile-creation", jsonValue);
                return outgoingAuthenticatedPlayerDTO;
            } else {
                throw new BadCredentialsException("Invalid username or password");
            }
        }

        throw new UsernameNotFoundException("Player not found");
    }

}

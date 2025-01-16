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
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public PlayerService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String getUsernameByUserId(UUID userId) {
        if (playerRepository.findById(userId).isPresent()) {
            return playerRepository.findById(userId).get().getUsername();
        }
        return null;
    }

    public UserDetails loadUserByUsername(String username) {
        if (playerRepository.findByUsername(username).isPresent()) {
            return (UserDetails) playerRepository.findByUsername(username).get();
        }
        return null;
    }


    public OutgoingPlayerDTO register(RegisterDTO registerDTO) {
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

        OutgoingPlayerDTO outgoingPlayerDTO = new OutgoingPlayerDTO();
        outgoingPlayerDTO.setFirstName(player.getFirstName());
        outgoingPlayerDTO.setUsername(player.getUsername());
        outgoingPlayerDTO.setRole(player.getRole());
        return outgoingPlayerDTO;
    }

    private void checkDuplicateUsername(String username) {
        if (playerRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException("Username already exists");
        }
    }

    private void checkDuplicateEmail(String email) {
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

    public OutgoingAuthenticatedPlayerDTO login(LoginDTO loginDTO) {

        if (playerRepository.findByUsername(loginDTO.getUsername().toLowerCase()).isPresent()) {
            Player player = playerRepository.findByUsername(loginDTO.getUsername().toLowerCase()).orElseThrow(() ->
                    new UsernameNotFoundException("Player not found"));
            if (passwordEncoder.matches(loginDTO.getPassword(), player.getPassword())) {
                List<String> authorities = new ArrayList<>();
                authorities.add(player.getRole());
                authorities.add(player.getUsername());

                OutgoingAuthenticatedPlayerDTO outgoingAuthenticatedPlayerDTO = new OutgoingAuthenticatedPlayerDTO();
                outgoingAuthenticatedPlayerDTO.setFirstName(player.getFirstName());
                outgoingAuthenticatedPlayerDTO.setUsername(player.getUsername());
                outgoingAuthenticatedPlayerDTO.setRole(player.getRole());
                outgoingAuthenticatedPlayerDTO.setToken(jwtUtil.generateToken(player, authorities));
                return outgoingAuthenticatedPlayerDTO;
            } else {
                throw new BadCredentialsException("Invalid username or password");
            }
        }

        throw new UsernameNotFoundException("Player not found");
    }
}

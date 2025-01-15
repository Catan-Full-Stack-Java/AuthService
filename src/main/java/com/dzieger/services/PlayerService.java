package com.dzieger.services;

import com.dzieger.dtos.OutgoingPlayerDTO;
import com.dzieger.dtos.RegisterDTO;
import com.dzieger.models.Player;
import com.dzieger.repositories.PlayerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
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
        return null;
    }
}

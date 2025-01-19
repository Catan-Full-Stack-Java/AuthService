package com.dzieger.services;

import com.dzieger.SecurityConfig.JwtUtil;
import com.dzieger.models.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlayerProfileService {

    private static final Logger log = LoggerFactory.getLogger(PlayerProfileService.class);

    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private static final String PLAYER_SERVICE_URL = "http://localhost:8081";

    public PlayerProfileService(RestTemplate restTemplate, JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.jwtUtil = jwtUtil;
    }

    @KafkaListener(topics = "profile-creation", groupId = "player-profile-service")
    public void createPlayerProfile(String token) {
        log.info("Creating player profile for player: {}", jwtUtil.extractUserId(token));

        String url = PLAYER_SERVICE_URL + "/api/v1/player/v1/profile";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Map<String, UUID>> request = new HttpEntity<>(Map.of("playerId", jwtUtil.extractUserId(token)), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.info("Player profile created successfully: {}", response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Failed to create player profile: {}", e.getMessage());
            // Implement retry logic
        }
    }
}


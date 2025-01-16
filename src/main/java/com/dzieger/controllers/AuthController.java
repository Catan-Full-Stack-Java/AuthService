package com.dzieger.controllers;

import com.dzieger.dtos.LoginDTO;
import com.dzieger.dtos.OutgoingAuthenticatedPlayerDTO;
import com.dzieger.dtos.OutgoingPlayerDTO;
import com.dzieger.dtos.RegisterDTO;
import com.dzieger.models.Player;
import com.dzieger.services.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final PlayerService playerService;

    public AuthController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<OutgoingPlayerDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(playerService.register(registerDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<OutgoingAuthenticatedPlayerDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(playerService.login(loginDTO));
    }

}

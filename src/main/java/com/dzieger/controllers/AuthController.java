package com.dzieger.controllers;

import com.dzieger.dtos.OutgoingPlayerDTO;
import com.dzieger.dtos.RegisterDTO;
import com.dzieger.models.Player;
import com.dzieger.services.PlayerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PlayerService playerService;

    public AuthController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public OutgoingPlayerDTO register(RegisterDTO registerDTO) {
        return playerService.register(registerDTO);
    }

}

package com.dzieger.controllers;

import com.dzieger.dtos.LoginDTO;
import com.dzieger.dtos.OutgoingAuthenticatedPlayerDTO;
import com.dzieger.dtos.OutgoingPlayerDTO;
import com.dzieger.dtos.RegisterDTO;
import com.dzieger.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Validated
@CrossOrigin
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final PlayerService playerService;

    public AuthController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Operation(summary = "Register a new player", description = "Register a new player with the given details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Player registered successfully."),
            @ApiResponse(responseCode = "400", description = "Validation error."),
            @ApiResponse(responseCode = "409", description = "Duplicate username or email.")
    })
    @PostMapping("/register")
    public ResponseEntity<OutgoingPlayerDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("Register endpoint hit");
        log.debug("Registering player: {}", registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.register(registerDTO));
    }

    @Operation(summary = "Login", description = "Login with the given credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful."),
            @ApiResponse(responseCode = "400", description = "Validation error."),
            @ApiResponse(responseCode = "401", description = "Bad credentials.")
    })
    @PostMapping("/login")
    public ResponseEntity<OutgoingAuthenticatedPlayerDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("Login endpoint hit");
        log.debug("Logging in player: {}", loginDTO);
        return ResponseEntity.ok(playerService.login(loginDTO));
    }

}

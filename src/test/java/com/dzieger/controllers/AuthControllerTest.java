package com.dzieger.controllers;

import com.dzieger.dtos.LoginDTO;
import com.dzieger.dtos.OutgoingAuthenticatedPlayerDTO;
import com.dzieger.dtos.OutgoingPlayerDTO;
import com.dzieger.dtos.RegisterDTO;
import com.dzieger.exceptions.GlobalExceptionHandler;
import com.dzieger.services.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testRegister_shouldReturnStatusOkAndOutgoingPlayerDTO() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("test");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("Valid@123");
        registerDTO.setFirstName("John");

        OutgoingPlayerDTO outgoingPlayerDTO = new OutgoingPlayerDTO();
        outgoingPlayerDTO.setUsername("test");
        outgoingPlayerDTO.setFirstName("John");
        outgoingPlayerDTO.setRole("PLAYER");

        when(playerService.register(any(RegisterDTO.class))).thenReturn(outgoingPlayerDTO);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"email\":\"test@email.com\",\"password\":\"Valid@123\",\"firstName\":\"John\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"username\":\"test\",\"firstName\":\"John\",\"role\":\"PLAYER\"}"))
                .andDo(print());
    }

    // Test register endpoint with invalid password
    @Test
    void testRegister_shouldReturnStatusBadRequest_whenPasswordIsInvalid() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("test");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("Valid@123");
        registerDTO.setFirstName("John");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"email\":\"test@email.com\",\"password\":\"invalid\",\"firstName\":\"John\"}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test register endpoint with invalid email
    @Test
    void testRegister_shouldReturnStatusBadRequest_whenEmailIsInvalid() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("test");
        registerDTO.setEmail("invalid");
        registerDTO.setPassword("Valid@123");
        registerDTO.setFirstName("John");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"email\":\"invalid\",\"password\":\"Valid@123\",\"firstName\":\"John\"}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test register endpoint with invalid username
    @Test
    void testRegister_shouldReturnStatusBadRequest_whenUsernameIsInvalid() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("Valid@123");
        registerDTO.setFirstName("John");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@email.com\",\"password\":\"Valid@123\",\"firstName\":\"John\"}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test register endpoint with invalid first name
    @Test
    void testRegister_shouldReturnStatusBadRequest_whenFirstNameIsInvalid() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("test");
        registerDTO.setEmail("test@email.com");
        registerDTO.setPassword("Valid@123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"email\":\"test@email.com\",\"password\":\"Valid@123\"}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test register endpoint with empty request body
    @Test
    void testRegister_shouldReturnStatusBadRequest_whenRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test login endpoint with valid credentials
    @Test
    void testLogin_shouldReturnStatusOkAndOutgoingAuthenticatedPlayerDTO() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("test");
        loginDTO.setPassword("Valid@123");

        OutgoingAuthenticatedPlayerDTO outgoingAuthenticatedPlayerDTO = new OutgoingAuthenticatedPlayerDTO();
        outgoingAuthenticatedPlayerDTO.setUsername("test");
        outgoingAuthenticatedPlayerDTO.setFirstName("John");
        outgoingAuthenticatedPlayerDTO.setRole("PLAYER");
        outgoingAuthenticatedPlayerDTO.setToken("token");

        when(playerService.login(any(LoginDTO.class))).thenReturn(outgoingAuthenticatedPlayerDTO);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"Valid@123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"test\",\"role\":\"PLAYER\",\"firstName\":\"John\",\"token\":\"token\"}"))
                .andDo(print());
    }

    // Test login endpoint with invalid credentials
    @Test
    void testLogin_shouldReturnStatusUnauthorized_whenCredentialsAreInvalid() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("test");
        loginDTO.setPassword("Invalid@123");

        when(playerService.login(any(LoginDTO.class))).thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"Invalid@123\"}"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    // Test login endpoint with empty request body
    @Test
    void testLogin_shouldReturnStatusBadRequest_whenRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test login endpoint with invalid username
    @Test
    void testLogin_shouldReturnStatusBadRequest_whenUsernameIsInvalid() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setPassword("Valid@123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"Valid@123\"}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}
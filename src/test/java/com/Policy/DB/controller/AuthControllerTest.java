package com.Policy.DB.controller;

import com.Policy.DB.dto.LoginRequest;
import com.Policy.DB.dto.LoginResponse;
import com.Policy.DB.dto.RegisterRequest;
import com.Policy.DB.model.User;
import com.Policy.DB.model.UserRole;
import com.Policy.DB.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------- LOGIN SUCCESS --------
    @Test
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("venkat", "password");

        LoginResponse response = new LoginResponse(
                "token123", "venkat", "v@gmail.com",
                UserRole.CUSTOMER, 1, "Login successful"
        );

        Mockito.when(authService.login(Mockito.any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"));
    }

    // -------- LOGIN FAILURE (catch) --------
    @Test
    void login_invalidCredentials() throws Exception {
        Mockito.when(authService.login(Mockito.any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("x", "y"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    // -------- REGISTER SUCCESS --------
    @Test
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "venkat", "v@gmail.com", "pass",
                UserRole.CUSTOMER, 1
        );

        LoginResponse response = new LoginResponse(
                "token", "venkat", "v@gmail.com",
                UserRole.CUSTOMER, 1, "Registration successful"
        );

        Mockito.when(authService.register(Mockito.any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    // -------- REGISTER FAILURE (catch) --------
    @Test
    void register_badRequest() throws Exception {
        Mockito.when(authService.register(Mockito.any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    // -------- VALIDATE TOKEN SUCCESS --------
    @Test
    void validateToken_success() throws Exception {
        User user = new User();
        user.setUsername("venkat");
        user.setRole(UserRole.ADMIN);

        Mockito.when(authService.getUserByToken("validToken"))
                .thenReturn(user);

        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "Bearer validToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("venkat"));
    }

    // -------- VALIDATE TOKEN FAILURE --------
    @Test
    void validateToken_invalid() throws Exception {
        Mockito.when(authService.getUserByToken("badToken"))
                .thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "Bearer badToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }
}

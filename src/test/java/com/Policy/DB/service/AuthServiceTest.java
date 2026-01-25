package com.Policy.DB.service;

import com.Policy.DB.dto.LoginRequest;
import com.Policy.DB.dto.RegisterRequest;
import com.Policy.DB.model.Customer;
import com.Policy.DB.model.User;
import com.Policy.DB.model.UserRole;
import com.Policy.DB.repository.CustomerRepository;
import com.Policy.DB.repository.UserRepository;
import com.Policy.DB.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    // -------- LOGIN SUCCESS --------
    @Test
    void login_success() {
        User user = new User();
        user.setUsername("venkat");
        user.setPassword("encoded");
        user.setEmail("v@gmail.com");
        user.setRole(UserRole.ADMIN);
        user.setIsActive(true);

        when(userRepository.findByUsername("venkat"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encoded"))
                .thenReturn(true);
        when(jwtUtil.generateToken("venkat"))
                .thenReturn("token");

        var response = authService.login(new LoginRequest("venkat", "pass"));

        assertEquals("venkat", response.getUsername());
        verify(userRepository).save(user);
    }

    // -------- LOGIN USER NOT FOUND --------
    @Test
    void login_userNotFound() {
        when(userRepository.findByUsername("x"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.login(new LoginRequest("x", "y")));
    }

    // -------- LOGIN INACTIVE --------
    @Test
    void login_inactiveAccount() {
        User user = new User();
        user.setIsActive(false);

        when(userRepository.findByUsername("v"))
                .thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> authService.login(new LoginRequest("v", "p")));
    }

    // -------- LOGIN WRONG PASSWORD --------
    @Test
    void login_wrongPassword() {
        User user = new User();
        user.setIsActive(true);
        user.setPassword("encoded");

        when(userRepository.findByUsername("v"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "encoded"))
                .thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> authService.login(new LoginRequest("v", "bad")));
    }

    // -------- REGISTER SUCCESS (ADMIN - no customer) --------
    @Test
    void register_adminSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setEmail("admin@gmail.com");
        request.setPassword("pass");
        request.setRole(UserRole.ADMIN);

        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(userRepository.existsByEmail("admin@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(jwtUtil.generateToken("admin")).thenReturn("token");

        var response = authService.register(request);

        assertEquals("Registration successful", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    // -------- REGISTER SUCCESS (CUSTOMER - auto-create) --------
    @Test
    void register_customerSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("customer");
        request.setEmail("customer@gmail.com");
        request.setPassword("pass");
        request.setRole(UserRole.CUSTOMER);
        request.setName("Customer Name");
        request.setPhone("1234567890");
        request.setAddress("123 Main St");

        Customer savedCustomer = new Customer();
        savedCustomer.setCustomerId(1);
        savedCustomer.setName("Customer Name");

        when(userRepository.existsByUsername("customer")).thenReturn(false);
        when(userRepository.existsByEmail("customer@gmail.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(jwtUtil.generateToken("customer")).thenReturn("token");

        var response = authService.register(request);

        assertEquals("Registration successful", response.getMessage());
        assertEquals(1, response.getCustomerId());
        verify(customerRepository).save(any(Customer.class));
        verify(userRepository).save(any(User.class));
    }

    // -------- REGISTER USERNAME EXISTS --------
    @Test
    void register_usernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicate");
        request.setEmail("e@gmail.com");
        request.setPassword("p");
        request.setRole(UserRole.ADMIN);

        when(userRepository.existsByUsername("duplicate")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> authService.register(request));
    }

    // -------- REGISTER EMAIL EXISTS --------
    @Test
    void register_emailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("duplicate@gmail.com");
        request.setPassword("p");
        request.setRole(UserRole.ADMIN);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("duplicate@gmail.com")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> authService.register(request));
    }

    // -------- REGISTER CUSTOMER WITHOUT NAME --------
    @Test
    void register_customerWithoutName() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("customer");
        request.setEmail("customer@gmail.com");
        request.setPassword("pass");
        request.setRole(UserRole.CUSTOMER);
        request.setPhone("1234567890");
        request.setAddress("123 Main St");
        // Missing name

        when(userRepository.existsByUsername("customer")).thenReturn(false);
        when(userRepository.existsByEmail("customer@gmail.com")).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> authService.register(request));
    }

    // -------- REGISTER CUSTOMER WITHOUT PHONE --------
    @Test
    void register_customerWithoutPhone() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("customer");
        request.setEmail("customer@gmail.com");
        request.setPassword("pass");
        request.setRole(UserRole.CUSTOMER);
        request.setName("Customer Name");
        request.setAddress("123 Main St");
        // Missing phone

        when(userRepository.existsByUsername("customer")).thenReturn(false);
        when(userRepository.existsByEmail("customer@gmail.com")).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> authService.register(request));
    }

    // -------- GET USER BY TOKEN --------
    @Test
    void getUserByToken_success() {
        User user = new User();
        user.setUsername("venkat");

        when(jwtUtil.getUsernameFromToken("token")).thenReturn("venkat");
        when(userRepository.findByUsername("venkat"))
                .thenReturn(Optional.of(user));

        User result = authService.getUserByToken("token");

        assertEquals("venkat", result.getUsername());
    }

    // -------- GET USER BY TOKEN FAIL --------
    @Test
    void getUserByToken_userNotFound() {
        when(jwtUtil.getUsernameFromToken("token")).thenReturn("x");
        when(userRepository.findByUsername("x"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.getUserByToken("token"));
    }
}
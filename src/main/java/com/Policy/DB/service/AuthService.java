package com.Policy.DB.service;
import com.Policy.DB.dto.LoginRequest;
import com.Policy.DB.dto.LoginResponse;
import com.Policy.DB.dto.RegisterRequest;
import com.Policy.DB.model.Customer;
import com.Policy.DB.model.User;
import com.Policy.DB.model.UserRole;
import com.Policy.DB.repository.UserRepository;
import com.Policy.DB.repository.CustomerRepository;
import com.Policy.DB.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!user.getIsActive()) {
            throw new RuntimeException("Account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCustomerId(),
                "Login successful"
        );
    }

    public LoginResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Integer customerId = null;

        // If role is CUSTOMER, create customer record first
        if (request.getRole() == UserRole.CUSTOMER) {
            // Validate required fields
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new RuntimeException("Name is required for customer registration");
            }
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                throw new RuntimeException("Phone is required for customer registration");
            }

            // Create new customer
            Customer customer = new Customer();
            customer.setName(request.getName());
            customer.setEmail(request.getEmail());
            customer.setPhone(request.getPhone());
            customer.setAddress(request.getAddress());

            Customer savedCustomer = customerRepository.save(customer);
            customerId = savedCustomer.getCustomerId();
        } else if (request.getRole() == UserRole.ADMIN) {
            // For admin registration, customerId should be null
            customerId = null;
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setCustomerId(customerId);
        user.setIsActive(true);

        userRepository.save(user);

        // Generate token
        String token = jwtUtil.generateToken(user.getUsername());

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCustomerId(),
                "Registration successful"
        );
    }

    public User getUserByToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
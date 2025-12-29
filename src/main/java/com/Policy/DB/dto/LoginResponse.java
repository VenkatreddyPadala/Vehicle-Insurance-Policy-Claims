package com.Policy.DB.dto;
import lombok.*;
import com.Policy.DB.model.UserRole;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private UserRole role;
    private Integer customerId;
    private String message;
}

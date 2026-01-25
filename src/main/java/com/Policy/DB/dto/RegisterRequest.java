package com.Policy.DB.dto;
import com.Policy.DB.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private UserRole role;
    private String name;
    private String phone;
    private String address;
    private Integer customerId;
}

package com.example.travel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateByAdminRequest {

    // Fields an admin might update, which a regular user might not be able to update for themselves

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username; // Admin might be allowed to change username

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    private String avatar;
    private LocalDate dateOfBirth;
    private String gender;
    private String city;
    private String country;

    private Boolean isActive; // Admin can activate/deactivate users
    private Boolean emailVerified; // Admin might verify email manually

    private Set<String> roles; // Admin can manage user roles (e.g., "USER", "ADMIN")

    private Set<Long> preferredTagIds; // Admin might also set preferred tags
}
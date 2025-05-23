package com.example.travel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    // Username usually not updatable, or handled separately
    // private String username;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    @Size(max = 255, message = "Avatar URL must be less than 255 characters")
    private String avatar;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Gender must be less than 20 characters")
    private String gender;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;

    // Password update should ideally be a separate endpoint/DTO for security reasons
    // private String password;

    private Set<Long> preferredTagIds; // IDs of preferred tags
}
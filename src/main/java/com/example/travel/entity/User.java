package com.example.travel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault; // For default boolean values

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") // "users" is a common table name, "user" can be a reserved keyword
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Column(nullable = false)
    @JsonIgnore // Typically, password should not be sent in responses
    private String password;

    @Column(nullable = false, unique = true)
    @Email(message = "Email should be valid")
    @Size(max = 100)
    private String email;

    @Size(max = 255)
    private String avatar; // URL to avatar image

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // New fields based on our discussion:

    @Size(max = 50)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 20) // e.g., MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    private String gender;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;

    @Column(name = "is_active")
    @ColumnDefault("true") // Set default value in DDL
    private boolean active = true; // Default value in Java object

    @Column(name = "email_verified")
    @ColumnDefault("false") // Set default value in DDL
    private boolean emailVerified = false; // Default value in Java object


    // Relationships

    @ElementCollection(fetch = FetchType.EAGER) // Roles are usually few and needed frequently
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Size(min = 1, message = "User must have at least one role") // Example validation
    private Set<String> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_preferred_tags",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnore // Avoid issues if Tag also has a back-reference to User
    private Set<Tag> preferredTags = new HashSet<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Avoid circular dependency and sending too much data
    private Set<UserFavorite> favorites = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Itinerary> itineraries = new HashSet<>();


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (roles.isEmpty()) { // Ensure every new user has at least a default role
            roles.add("ROLE_USER");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Lombok's @Data will generate getters, setters, toString, equals, hashCode.
    // If you need custom logic in getters/setters, you might remove @Data and generate them manually.
    // For example, to add a user to a role:
    public void addRole(String role) {
        this.roles.add(role);
    }

    public void removeRole(String role) {
        this.roles.remove(role);
    }

    public void addPreferredTag(Tag tag) {
        this.preferredTags.add(tag);
        tag.getUsersWithThisPreference().add(this); // Assuming Tag entity has a Set<User> usersWithThisPreference field
    }

    public void removePreferredTag(Tag tag) {
        this.preferredTags.remove(tag);
        tag.getUsersWithThisPreference().remove(this); // Assuming Tag entity has a Set<User> usersWithThisPreference field
    }
}
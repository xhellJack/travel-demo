package com.example.travel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Tag name cannot be blank")
    @Size(min = 1, max = 50, message = "Tag name must be between 1 and 50 characters")
    private String name;

    @Column(length = 50) // Renamed from 'category' in thought process to 'tag_category' for clarity in DB
    @Size(max = 50, message = "Tag category must be less than 50 characters")
    private String tagCategory; // e.g., "Interest", "Activity", "Cuisine"

    @Column(length = 500)
    @Size(max = 500, message = "Tag description must be less than 500 characters")
    private String description;

    // Relationship with AttractionTag (Many-to-Many with Attraction through AttractionTag entity)
    // This remains as per your existing structure.

    // Bidirectional relationship with User (for user preferences)
    @ManyToMany(mappedBy = "preferredTags", fetch = FetchType.LAZY)
    @JsonIgnore // Avoid circular dependency and performance issues
    private Set<User> usersWithThisPreference = new HashSet<>();

    // Lombok will generate constructors, getters, setters, etc.

    // Helper methods for managing bidirectional relationship with User, if needed on Tag side
    // (though typically managed by the owning side, User in this case for preferredTags)
    public void addUserPreference(User user) {
        this.usersWithThisPreference.add(user);
        // No need to call user.getPreferredTags().add(this) here,
        // as User entity's addPreferredTag method should handle the bidirectional link.
    }

    public void removeUserPreference(User user) {
        this.usersWithThisPreference.remove(user);
        // Similar to addUserPreference, User entity should manage its side.
    }
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @JsonIgnore // Avoid circular dependency and performance issues
    private Set<Attraction> attractions = new HashSet<>();


    // REMOVED: The @OneToMany relationship to AttractionTag
    // @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    // @JsonIgnore
    // private Set<AttractionTag> attractionTags = new HashSet<>();


    // Lombok will generate constructors, getters, setters, etc.

    // Helper methods for managing bidirectional relationship with Attraction
    // (Typically, the owning side - Attraction - manages adding/removing tags,
    // but these can be useful for completeness or specific scenarios on the Tag side)
    public void addAttraction(Attraction attraction) {
        this.attractions.add(attraction);
        // No need to call attraction.getTags().add(this) if Attraction's addTag handles it
    }

    public void removeAttraction(Attraction attraction) {
        this.attractions.remove(attraction);
        // No need to call attraction.getTags().remove(this) if Attraction's removeTag handles it
    }
}
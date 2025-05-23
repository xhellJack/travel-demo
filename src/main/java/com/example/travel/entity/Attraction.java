package com.example.travel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault; // For default rating values

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attractions")
public class Attraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Attraction name cannot be blank")
    @Size(max = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255) // General location, might be a city or region
    @Size(max = 255)
    private String location;

    @Column(name = "opening_hours", length = 255)
    @Size(max = 255)
    private String openingHours;

    @Column(name = "ticket_price", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Ticket price must be non-negative")
    private BigDecimal ticketPrice;

    @Column(name = "image_url", length = 500)
    @Size(max = 500)
    // Consider @URL validation if using hibernate-validator
    private String imageUrl;

    private Double latitude;
    private Double longitude;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // New/Updated fields:

    @Column(length = 50, nullable = false) // Made category non-nullable as it's a primary classification
    @NotBlank(message = "Attraction category cannot be blank")
    @Size(max = 50)
    private String category; // e.g., "Historic Site", "Museum", "Park"

    @Column(name = "average_rating")
    @ColumnDefault("0.0")
    @Min(value = 0, message = "Average rating cannot be less than 0")
    @Max(value = 5, message = "Average rating cannot be more than 5")
    private Double averageRating = 0.0;

    @Column(name = "rating_count")
    @ColumnDefault("0")
    @Min(value = 0, message = "Rating count cannot be less than 0")
    private Integer ratingCount = 0;

    @Column(length = 255)
    @Size(max = 255)
    private String address; // Detailed address

    @Column(name = "contact_phone", length = 30)
    @Size(max = 30)
    private String contactPhone;

    @Column(length = 255)
    @Size(max = 255)
    // Consider @URL from org.hibernate.validator.constraints.URL
    private String website;

    @Column(name = "estimated_duration_hours", precision = 4, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Estimated duration must be non-negative")
    private BigDecimal estimatedDurationHours;

    @Column(name = "best_time_to_visit", length = 100)
    @Size(max = 100)
    private String bestTimeToVisit;

    @Column(length = 30)
    @Size(max = 30)
    private String status; // e.g., "OPEN", "CLOSED_TEMPORARILY"


    // Relationships

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "attraction_tags_join", // Explicit join table name
            joinColumns = @JoinColumn(name = "attraction_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnore // Avoid circular dependency if Tag also refers back to Attraction
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "attraction", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<UserFavorite> favorites = new HashSet<>();

    @OneToMany(mappedBy = "attraction", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "attraction", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // This links Attraction to its occurrences in different itineraries
    private Set<ItineraryAttraction> itineraryAttractions = new HashSet<>();


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (this.averageRating == null) this.averageRating = 0.0;
        if (this.ratingCount == null) this.ratingCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for managing tags (if Attraction owns the relationship)
    public void addTag(Tag tag) {
        this.tags.add(tag);
        // If Tag entity had a Set<Attraction> attractions, you would add this attraction to it:
        // tag.getAttractions().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        // If Tag entity had a Set<Attraction> attractions, you would remove this attraction from it:
        // tag.getAttractions().remove(this);
    }
}
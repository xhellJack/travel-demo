package com.example.travel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "itineraries")
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "favorites", "reviews", "itineraries", "preferredTags", "roles"})
    private User user;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Itinerary name cannot be blank")
    @Size(max = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    @Size(max = 5000)
    private String description;

    @Column(name = "start_date")
    // Consider adding custom validation: startDate must be before or same as endDate
    private LocalDate startDate;

    @Column(name = "end_date")
    // Consider adding custom validation: endDate must be after or same as startDate
    private LocalDate endDate;

    @Column(name = "is_public")
    @ColumnDefault("false")
    private boolean isPublic = false;

    @Column(length = 30)
    @Size(max = 30)
    private String status; // e.g., "PLANNING", "CONFIRMED", "COMPLETED", "CANCELLED"

    @Column(name = "total_estimated_cost", precision = 12, scale = 2)
    @PositiveOrZero(message = "Estimated cost must be non-negative")
    private BigDecimal totalEstimatedCost;

    @Column(name = "cover_image_url", length = 500)
    @Size(max = 500)
    // Consider @URL validation
    private String coverImageUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationship with Tags
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "itinerary_tags_join",
            joinColumns = @JoinColumn(name = "itinerary_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnore // Typically don't load all tags with itinerary list, can fetch on detail view
    private Set<Tag> tags = new HashSet<>();

    // One itinerary has many itinerary-attraction links
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonIgnore // Usually want to load these when fetching a specific itinerary
    // For itinerary details, you'd likely want this. For lists, maybe ignore.
    // Consider using DTOs to control what gets serialized.
    // For now, let's keep it unignored for detail retrieval, but LAZY fetching.
    private Set<ItineraryAttraction> itineraryAttractions = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (isPublic == false && status == null) { // Default status for new private itineraries
            status = "PLANNING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for managing tags
    public void addTag(Tag tag) {
        this.tags.add(tag);
        // Assuming Tag does not have a direct Set<Itinerary> mappedBy this field.
        // If it did, you'd add: tag.getItineraries().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        // if (tag.getItineraries() != null) tag.getItineraries().remove(this);
    }

    // Helper for adding an attraction to the itinerary (complex object, usually done via service)
    public void addItineraryAttraction(ItineraryAttraction itineraryAttraction) {
        this.itineraryAttractions.add(itineraryAttraction);
        itineraryAttraction.setItinerary(this);
    }

    public void removeItineraryAttraction(ItineraryAttraction itineraryAttraction) {
        this.itineraryAttractions.remove(itineraryAttraction);
        itineraryAttraction.setItinerary(null);
    }
}
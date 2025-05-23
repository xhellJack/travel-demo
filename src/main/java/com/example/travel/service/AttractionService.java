package com.example.travel.service;

import com.example.travel.dto.AttractionCreateRequest;
import com.example.travel.dto.AttractionResponse;
import com.example.travel.dto.AttractionUpdateRequest;
import com.example.travel.dto.TagResponse; // Assuming TagService provides this or similar
import com.example.travel.entity.Attraction;
import com.example.travel.entity.Tag;
import com.example.travel.entity.User;
import com.example.travel.entity.UserActivityLog;
import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.repository.AttractionRepository;
import com.example.travel.repository.ReviewRepository; // Needed for calculating average rating
import com.example.travel.repository.UserActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // For StringUtils.hasText

import jakarta.persistence.criteria.Predicate; // For JPA Criteria API
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final TagService tagService; // Inject TagService for tag operations and DTO conversion
    private final ReviewRepository reviewRepository; // For calculating average rating

    private final UserActivityLogRepository userActivityLogRepository;
    private final UserService userService; // To get current authenticated user if not passed directly
    @Autowired
    public AttractionService(AttractionRepository attractionRepository,
                             TagService tagService,
                             ReviewRepository reviewRepository,
                             UserActivityLogRepository userActivityLogRepository,
                             UserService userService) {
        this.attractionRepository = attractionRepository;
        this.tagService = tagService;
        this.reviewRepository = reviewRepository;
        this.userActivityLogRepository = userActivityLogRepository;
        this.userService = userService;
    }


    // --- DTO Converter ---
    public AttractionResponse convertToAttractionResponse(Attraction attraction) {
        if (attraction == null) {
            return null;
        }
        AttractionResponse response = new AttractionResponse();
        response.setId(attraction.getId());
        response.setName(attraction.getName());
        response.setDescription(attraction.getDescription());
        response.setLocation(attraction.getLocation());
        response.setAddress(attraction.getAddress());
        response.setOpeningHours(attraction.getOpeningHours());
        response.setTicketPrice(attraction.getTicketPrice());
        response.setImageUrl(attraction.getImageUrl());
        response.setLatitude(attraction.getLatitude());
        response.setLongitude(attraction.getLongitude());
        response.setCategory(attraction.getCategory());
        response.setAverageRating(attraction.getAverageRating());
        response.setRatingCount(attraction.getRatingCount());
        response.setContactPhone(attraction.getContactPhone());
        response.setWebsite(attraction.getWebsite());
        response.setEstimatedDurationHours(attraction.getEstimatedDurationHours());
        response.setBestTimeToVisit(attraction.getBestTimeToVisit());
        response.setStatus(attraction.getStatus());
        response.setCreatedAt(attraction.getCreatedAt());
        response.setUpdatedAt(attraction.getUpdatedAt());

        if (attraction.getTags() != null) {
            response.setTags(tagService.convertToTagResponseSet(attraction.getTags()));
        } else {
            response.setTags(new HashSet<>());
        }
        return response;
    }

    private Page<AttractionResponse> convertToAttractionResponsePage(Page<Attraction> attractionPage) {
        return attractionPage.map(this::convertToAttractionResponse);
    }


    // --- CRUD Operations ---

    @Transactional
    @CacheEvict(value = {"popularAttractions", "attractionSearch"}, allEntries = true) // Evict cache on create
    public AttractionResponse createAttraction(AttractionCreateRequest request) {
        Attraction attraction = new Attraction();
        mapRequestToAttraction(request, attraction); // Helper to map fields

        // Handle Tags
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = tagService.findTagsByIds(request.getTagIds());
            attraction.setTags(tags);
        }

        // Initialize rating fields
        attraction.setAverageRating(0.0);
        attraction.setRatingCount(0);

        Attraction savedAttraction = attractionRepository.save(attraction);
        return convertToAttractionResponse(savedAttraction);
    }

    @Transactional(readOnly = true)
    public AttractionResponse getAttractionById(Long id) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found with id: " + id));

        // Log activity (if user is authenticated)
        User currentUser = userService.getCurrentAuthenticatedUser(); // Assuming this method handles unauthenticated users gracefully or is called in a secured context
        if (currentUser != null) {
            UserActivityLog log = new UserActivityLog(currentUser, attraction, "VIEW_ATTRACTION", "Viewed attraction details for ID: " + id);
            userActivityLogRepository.save(log);
        }
        return convertToAttractionResponse(attraction);
    }


    @Transactional(readOnly = true)
    public Attraction findAttractionEntityById(Long id) { // Helper for other services
        return attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found with id: " + id));
    }


    @Transactional
    @CacheEvict(value = {"popularAttractions", "attractionSearch"}, allEntries = true) // Evict cache on update
    public AttractionResponse updateAttraction(Long id, AttractionUpdateRequest request) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found with id: " + id));

        mapRequestToAttraction(request, attraction); // Helper to map fields (can be a common private method)

        // Handle Tags update
        if (request.getTagIds() != null) { // Allow sending empty set to clear tags
            Set<Tag> tags = new HashSet<>();
            if (!request.getTagIds().isEmpty()) {
                tags = tagService.findTagsByIds(request.getTagIds());
            }
            attraction.setTags(tags);
        }

        Attraction updatedAttraction = attractionRepository.save(attraction);
        return convertToAttractionResponse(updatedAttraction);
    }

    @Transactional
    @CacheEvict(value = {"popularAttractions", "attractionSearch"}, allEntries = true) // Evict cache on delete
    public void deleteAttraction(Long id) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found with id: " + id));

        // Consider implications:
        // - Reviews related to this attraction? (Usually kept, or soft delete attraction)
        // - Favorites related to this attraction? (CASCADE delete by DB on user_favorites if attraction_id FK is set up, or manually remove)
        // - ItineraryAttractions? (CASCADE delete by DB on itinerary_attractions if attraction_id FK is set up, or manually remove)
        // For now, a direct delete. The @ManyToMany tags relationship will have its join table entries removed.
        attractionRepository.delete(attraction);
    }

    // --- Search and other methods ---

    @Transactional(readOnly = true)
    @Cacheable(value = "attractionSearch", key = "#keyword + '-' + #category + '-' + #minRating + '-' + #tagIds?.toString() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<AttractionResponse> searchAttractions(
            String keyword, String category, Float minRating, List<Long> tagIds, Pageable pageable) {

        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser != null) {
            String searchDetails = String.format("Keyword: %s, Category: %s, MinRating: %s, TagIds: %s", keyword, category, minRating, tagIds);
            UserActivityLog log = new UserActivityLog(currentUser, null, "SEARCH_ATTRACTIONS", searchDetails); // No specific attraction for a search query
            userActivityLogRepository.save(log);
        }
        Specification<Attraction> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("category")), category.toLowerCase()));
            }
            if (minRating != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }
            if (tagIds != null && !tagIds.isEmpty()) {
                // This creates a subquery or join to filter by tags.
                // For each tagId, we need to check if the attraction's tags collection contains a tag with that ID.
                // A common way is to join and check:
                predicates.add(root.join("tags").get("id").in(tagIds));
                // If you need attractions that have ALL specified tags, the query is more complex.
                // This 'in' means it has ANY of the specified tags.
            }
            // Add other filters: location, status etc.
            // Example: if you add a 'location' parameter for search
            // if (StringUtils.hasText(location)) {
            //     predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            // }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Attraction> attractionsPage = attractionRepository.findAll(spec, pageable);
        return convertToAttractionResponsePage(attractionsPage);
    }

    // In AttractionService.java
    @Transactional(readOnly = true)
    @Cacheable("popularAttractionsPage") // Use a different cache name if signature changes
    public Page<AttractionResponse> getPopularAttractions(Pageable pageable) { // Changed parameter to Pageable
        Page<Attraction> popularPage = attractionRepository.findByOrderByAverageRatingDescRatingCountDesc(pageable);
        return popularPage.map(this::convertToAttractionResponse); // Use the existing page map function
    }

    // Method to be called by ReviewService when a review is added/updated/deleted
    @Transactional
    @CacheEvict(value = {"popularAttractions", "attractionSearch"}, allEntries = true)
    public void updateAttractionRatingAndCount(Long attractionId) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found with id: " + attractionId + " for rating update."));

        Double newAverageRating = reviewRepository.calculateAverageRatingByAttractionId(attractionId);
        Integer newRatingCount = reviewRepository.countReviewsByAttractionId(attractionId);

        attraction.setAverageRating(newAverageRating != null ? newAverageRating : 0.0);
        attraction.setRatingCount(newRatingCount != null ? newRatingCount : 0);
        attractionRepository.save(attraction);
    }

    // --- Private Helper Methods for mapping DTO to Entity ---
    private void mapRequestToAttraction(AttractionCreateRequest request, Attraction attraction) {
        // Common fields for create
        attraction.setName(request.getName());
        attraction.setDescription(request.getDescription());
        attraction.setLocation(request.getLocation());
        attraction.setAddress(request.getAddress());
        attraction.setOpeningHours(request.getOpeningHours());
        attraction.setTicketPrice(request.getTicketPrice());
        attraction.setImageUrl(request.getImageUrl());
        attraction.setLatitude(request.getLatitude());
        attraction.setLongitude(request.getLongitude());
        attraction.setCategory(request.getCategory());
        attraction.setContactPhone(request.getContactPhone());
        attraction.setWebsite(request.getWebsite());
        attraction.setEstimatedDurationHours(request.getEstimatedDurationHours());
        attraction.setBestTimeToVisit(request.getBestTimeToVisit());
        attraction.setStatus(request.getStatus());
    }

    private void mapRequestToAttraction(AttractionUpdateRequest request, Attraction attraction) {
        // Fields for update (check for null before setting, as they are optional)
        if (request.getName() != null) attraction.setName(request.getName());
        if (request.getDescription() != null) attraction.setDescription(request.getDescription());
        if (request.getLocation() != null) attraction.setLocation(request.getLocation());
        if (request.getAddress() != null) attraction.setAddress(request.getAddress());
        if (request.getOpeningHours() != null) attraction.setOpeningHours(request.getOpeningHours());
        if (request.getTicketPrice() != null) attraction.setTicketPrice(request.getTicketPrice());
        if (request.getImageUrl() != null) attraction.setImageUrl(request.getImageUrl());
        if (request.getLatitude() != null) attraction.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) attraction.setLongitude(request.getLongitude());
        if (request.getCategory() != null) attraction.setCategory(request.getCategory());
        if (request.getContactPhone() != null) attraction.setContactPhone(request.getContactPhone());
        if (request.getWebsite() != null) attraction.setWebsite(request.getWebsite());
        if (request.getEstimatedDurationHours() != null) attraction.setEstimatedDurationHours(request.getEstimatedDurationHours());
        if (request.getBestTimeToVisit() != null) attraction.setBestTimeToVisit(request.getBestTimeToVisit());
        if (request.getStatus() != null) attraction.setStatus(request.getStatus());
    }
}
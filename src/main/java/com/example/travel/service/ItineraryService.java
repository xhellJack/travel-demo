package com.example.travel.service;

import com.example.travel.dto.*; // Import all DTOs
import com.example.travel.entity.*; // Import all Entities
import com.example.travel.exception.ForbiddenException;
import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.repository.ItineraryRepository;
// ItineraryAttractionRepository might not be directly needed if managing through Itinerary's collection
// import com.example.travel.repository.ItineraryAttractionRepository;
import com.example.travel.repository.UserActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final UserService userService;
    private final AttractionService attractionService;
    private final TagService tagService;

    private final UserActivityLogRepository userActivityLogRepository;
    // private final ItineraryAttractionRepository itineraryAttractionRepository; // May not be needed

    @Autowired
    public ItineraryService(ItineraryRepository itineraryRepository,
                            UserService userService,
                            AttractionService attractionService,
                            TagService tagService,
                            UserActivityLogRepository userActivityLogRepository
            /* ItineraryAttractionRepository itineraryAttractionRepository */) {
        this.itineraryRepository = itineraryRepository;
        this.userService = userService;
        this.attractionService = attractionService;
        this.tagService = tagService;
        this.userActivityLogRepository = userActivityLogRepository;
        // this.itineraryAttractionRepository = itineraryAttractionRepository;
    }

    // --- DTO Converters ---

    public ItineraryResponse convertToItineraryResponse(Itinerary itinerary) {
        if (itinerary == null) return null;
        ItineraryResponse response = new ItineraryResponse();
        response.setId(itinerary.getId());
        response.setName(itinerary.getName());
        response.setDescription(itinerary.getDescription());
        response.setStartDate(itinerary.getStartDate());
        response.setEndDate(itinerary.getEndDate());
        response.setIsPublic(itinerary.isPublic());
        response.setStatus(itinerary.getStatus());
        response.setTotalEstimatedCost(itinerary.getTotalEstimatedCost());
        response.setCoverImageUrl(itinerary.getCoverImageUrl());
        response.setCreatedAt(itinerary.getCreatedAt());
        response.setUpdatedAt(itinerary.getUpdatedAt());

        if (itinerary.getUser() != null) {
            // Assuming simple UserBasicResponse mapping here
            response.setUser(new UserBasicResponse(
                    itinerary.getUser().getId(),
                    itinerary.getUser().getUsername(),
                    itinerary.getUser().getAvatar()
            ));
        }

        if (itinerary.getTags() != null) {
            response.setTags(tagService.convertToTagResponseSet(itinerary.getTags()));
        } else {
            response.setTags(new HashSet<>());
        }

        if (itinerary.getItineraryAttractions() != null) {
            response.setItineraryAttractions(itinerary.getItineraryAttractions().stream()
                    .map(this::convertToItineraryAttractionResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setItineraryAttractions(new ArrayList<>());
        }
        return response;
    }

    private ItineraryAttractionResponse convertToItineraryAttractionResponse(ItineraryAttraction ia) {
        if (ia == null) return null;
        ItineraryAttractionResponse response = new ItineraryAttractionResponse();
        if (ia.getAttraction() != null) {
            // Assuming simple AttractionBasicResponse mapping here
            response.setAttraction(new AttractionBasicResponse(
                    ia.getAttraction().getId(),
                    ia.getAttraction().getName(),
                    ia.getAttraction().getImageUrl(),
                    ia.getAttraction().getCategory(),
                    ia.getAttraction().getLocation(),
                    ia.getAttraction().getAverageRating()
            ));
        }
        response.setVisitDate(ia.getVisitDate());
        response.setOrderInItinerary(ia.getOrderInItinerary());
        response.setStartTime(ia.getStartTime());
        response.setEndTime(ia.getEndTime());
        response.setCustomCost(ia.getCustomCost());
        response.setTransportationToNextNotes(ia.getTransportationToNextNotes());
        response.setNotes(ia.getNotes());
        return response;
    }

    private ItineraryBasicResponse convertToItineraryBasicResponse(Itinerary itinerary) {
        if (itinerary == null) return null;
        ItineraryBasicResponse basicResponse = new ItineraryBasicResponse();
        basicResponse.setId(itinerary.getId());
        basicResponse.setName(itinerary.getName());
        basicResponse.setCoverImageUrl(itinerary.getCoverImageUrl());
        basicResponse.setStartDate(itinerary.getStartDate());
        basicResponse.setEndDate(itinerary.getEndDate());
        basicResponse.setIsPublic(itinerary.isPublic());
        basicResponse.setStatus(itinerary.getStatus());
        if (itinerary.getUser() != null) {
            basicResponse.setUser(new UserBasicResponse(
                    itinerary.getUser().getId(),
                    itinerary.getUser().getUsername(),
                    itinerary.getUser().getAvatar()
            ));
        }
        // Potentially add numberOfDays, numberOfAttractions
        return basicResponse;
    }

    private Page<ItineraryBasicResponse> convertToItineraryBasicResponsePage(Page<Itinerary> itineraryPage) {
        return itineraryPage.map(this::convertToItineraryBasicResponse);
    }

    // --- Service Methods ---

    @Transactional
    public ItineraryResponse createItinerary(ItineraryCreateRequest request, Long userId) {
        User user = userService.findUserEntityById(userId); // Ensures user exists

        Itinerary itinerary = new Itinerary();
        itinerary.setUser(user);
        itinerary.setName(request.getName());
        itinerary.setDescription(request.getDescription());
        itinerary.setStartDate(request.getStartDate());
        itinerary.setEndDate(request.getEndDate());
        itinerary.setPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        itinerary.setStatus(request.getStatus() != null ? request.getStatus() : "PLANNING");
        itinerary.setTotalEstimatedCost(request.getTotalEstimatedCost());
        itinerary.setCoverImageUrl(request.getCoverImageUrl());

        // Handle Tags
        if (!CollectionUtils.isEmpty(request.getTagIds())) {
            Set<Tag> tags = tagService.findTagsByIds(request.getTagIds());
            itinerary.setTags(tags);
        }

        // Handle ItineraryAttractions
        if (!CollectionUtils.isEmpty(request.getItineraryAttractions())) {
            Set<ItineraryAttraction> itineraryAttractionsSet = new HashSet<>();
            for (ItineraryAttractionRequest iaRequest : request.getItineraryAttractions()) {
                Attraction attraction = attractionService.findAttractionEntityById(iaRequest.getAttractionId());
                ItineraryAttraction ia = new ItineraryAttraction();
                // The ID (ItineraryAttractionId) fields (itinerary, attraction) will be set by JPA through relationship
                // Or, if you need to manually set the ID for some reason before save (usually not for composite keys like this with @IdClass):
                // ItineraryAttractionId iaId = new ItineraryAttractionId(null, attraction.getId()); // Itinerary ID is not known yet
                // ia.setId(iaId); // Not typical for @IdClass where fields are marked @Id

                ia.setItinerary(itinerary); // Link back to the itinerary
                ia.setAttraction(attraction);
                ia.setVisitDate(iaRequest.getVisitDate());
                ia.setOrderInItinerary(iaRequest.getOrderInItinerary());
                ia.setStartTime(iaRequest.getStartTime());
                ia.setEndTime(iaRequest.getEndTime());
                ia.setCustomCost(iaRequest.getCustomCost());
                ia.setTransportationToNextNotes(iaRequest.getTransportationToNextNotes());
                ia.setNotes(iaRequest.getNotes());
                itineraryAttractionsSet.add(ia);
            }
            itinerary.setItineraryAttractions(itineraryAttractionsSet);
        }

        Itinerary savedItinerary = itineraryRepository.save(itinerary);
        return convertToItineraryResponse(savedItinerary);
    }

    @Transactional(readOnly = true)
    public ItineraryResponse getItineraryById(Long itineraryId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found with id: " + itineraryId));

        // Optional: Permission check if itinerary is not public
        // User currentUser = userService.getCurrentAuthenticatedUser();
        // if (!itinerary.isPublic() && (currentUser == null || !currentUser.getId().equals(itinerary.getUser().getId()))) {
        //     // Add admin check if admin can view any
        //     throw new ForbiddenException("You are not authorized to view this private itinerary.");
        // }
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser != null) {
            UserActivityLog log = new UserActivityLog(currentUser, null, "VIEW_ITINERARY", "Viewed itinerary details for ID: " + itineraryId);
            userActivityLogRepository.save(log);
        }
        return convertToItineraryResponse(itinerary);
    }

    @Transactional(readOnly = true)
    public Itinerary findItineraryEntityById(Long itineraryId) { // For internal use
        return itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found with id: " + itineraryId));
    }


    @Transactional(readOnly = true)
    public Page<ItineraryBasicResponse> getItinerariesByUserId(Long userId, Pageable pageable) {
        userService.findUserEntityById(userId); // Validate user exists
        Page<Itinerary> itineraryPage = itineraryRepository.findByUserId(userId, pageable);
        return convertToItineraryBasicResponsePage(itineraryPage);
    }

    @Transactional(readOnly = true)
    public Page<ItineraryBasicResponse> getPublicItineraries(Pageable pageable) {
        Page<Itinerary> itineraryPage = itineraryRepository.findByIsPublicTrue(pageable);
        return convertToItineraryBasicResponsePage(itineraryPage);
    }


    @Transactional
    public ItineraryResponse updateItinerary(Long itineraryId, ItineraryUpdateRequest request, Long currentUserId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found with id: " + itineraryId));

        // Authorization check
        if (!itinerary.getUser().getId().equals(currentUserId)) {
            // Allow admin to update? Add role check here if needed.
            throw new ForbiddenException("You are not authorized to update this itinerary.");
        }

        // Update basic fields
        if (request.getName() != null) itinerary.setName(request.getName());
        if (request.getDescription() != null) itinerary.setDescription(request.getDescription());
        if (request.getStartDate() != null) itinerary.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) itinerary.setEndDate(request.getEndDate());
        if (request.getIsPublic() != null) itinerary.setPublic(request.getIsPublic());
        if (request.getStatus() != null) itinerary.setStatus(request.getStatus());
        if (request.getTotalEstimatedCost() != null) itinerary.setTotalEstimatedCost(request.getTotalEstimatedCost());
        if (request.getCoverImageUrl() != null) itinerary.setCoverImageUrl(request.getCoverImageUrl());

        // Update Tags
        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            if (!request.getTagIds().isEmpty()) {
                tags = tagService.findTagsByIds(request.getTagIds());
            }
            itinerary.setTags(tags);
        }

        // Update ItineraryAttractions (using full replacement strategy for simplicity)
        if (request.getItineraryAttractions() != null) {
            // Clear existing itinerary attractions
            itinerary.getItineraryAttractions().clear();
            // itineraryAttractionRepository.deleteByItineraryId(itineraryId); // Alternative if not using orphanRemoval or direct collection management

            Set<ItineraryAttraction> newItineraryAttractionsSet = new HashSet<>();
            for (ItineraryAttractionRequest iaRequest : request.getItineraryAttractions()) {
                Attraction attraction = attractionService.findAttractionEntityById(iaRequest.getAttractionId());
                ItineraryAttraction ia = new ItineraryAttraction();
                ia.setItinerary(itinerary);
                ia.setAttraction(attraction);
                ia.setVisitDate(iaRequest.getVisitDate());
                ia.setOrderInItinerary(iaRequest.getOrderInItinerary());
                ia.setStartTime(iaRequest.getStartTime());
                ia.setEndTime(iaRequest.getEndTime());
                ia.setCustomCost(iaRequest.getCustomCost());
                ia.setTransportationToNextNotes(iaRequest.getTransportationToNextNotes());
                ia.setNotes(iaRequest.getNotes());
                newItineraryAttractionsSet.add(ia);
            }
            itinerary.getItineraryAttractions().addAll(newItineraryAttractionsSet);
        }

        Itinerary updatedItinerary = itineraryRepository.save(itinerary);
        return convertToItineraryResponse(updatedItinerary);
    }

    @Transactional
    public void deleteItinerary(Long itineraryId, Long currentUserId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found with id: " + itineraryId));

        // Authorization check
        User currentUser = userService.getCurrentAuthenticatedUser();
        boolean isAdmin = currentUser != null && currentUser.getRoles().contains("ADMIN"); // Adjust role string

        if (!itinerary.getUser().getId().equals(currentUserId) && !isAdmin) {
            throw new ForbiddenException("You are not authorized to delete this itinerary.");
        }

        // Deleting the itinerary should also delete its ItineraryAttractions due to
        // CascadeType.ALL and orphanRemoval=true on Itinerary.itineraryAttractions
        itineraryRepository.delete(itinerary);
    }
}
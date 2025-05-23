package com.example.travel.service;

import com.example.travel.dto.AttractionResponse; // To return list of favorite attractions
import com.example.travel.entity.Attraction;
import com.example.travel.entity.User;
import com.example.travel.entity.UserFavorite;
import com.example.travel.entity.UserFavoriteId;
import com.example.travel.exception.ConflictException;
import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.repository.UserFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;
    private final UserService userService;
    private final AttractionService attractionService;

    @Autowired
    public FavoriteService(UserFavoriteRepository userFavoriteRepository,
                           UserService userService,
                           AttractionService attractionService) {
        this.userFavoriteRepository = userFavoriteRepository;
        this.userService = userService;
        this.attractionService = attractionService;
    }

    @Transactional
    public void addFavorite(Long userId, Long attractionId) {
        // Validate user and attraction existence (these methods throw ResourceNotFoundException if not found)
        User user = userService.findUserEntityById(userId);
        Attraction attraction = attractionService.findAttractionEntityById(attractionId);

        // Check if already favorited
        if (userFavoriteRepository.existsByUser_IdAndAttraction_Id(userId, attractionId)) {
            throw new ConflictException("Attraction with id " + attractionId + " is already in favorites for user " + userId);
        }

        UserFavorite userFavorite = new UserFavorite();
        // UserFavoriteId id = new UserFavoriteId(userId, attractionId); // UserFavoriteId fields are user and attraction (Long type)
        // userFavorite.setId(id); // Not needed if @IdClass is used directly on User and Attraction fields in UserFavorite entity

        userFavorite.setUser(user);
        userFavorite.setAttraction(attraction);
        // userFavorite.setCreatedAt(LocalDateTime.now()); // Already handled by @PrePersist in UserFavorite entity

        userFavoriteRepository.save(userFavorite);
    }

    @Transactional
    public void removeFavorite(Long userId, Long attractionId) {
        // Validate user and attraction existence (optional, as delete operation might not find anything)
        // userService.findUserEntityById(userId);
        // attractionService.findAttractionEntityById(attractionId);

        if (!userFavoriteRepository.existsByUser_IdAndAttraction_Id(userId, attractionId)) {
            throw new ResourceNotFoundException("Favorite entry not found for user " + userId + " and attraction " + attractionId);
        }
        // UserFavoriteId id = new UserFavoriteId(userId, attractionId);
        // userFavoriteRepository.deleteById(id); // Requires findById first if only id is passed.
        // Or use a custom delete method in repository
        userFavoriteRepository.deleteByUser_IdAndAttraction_Id(userId, attractionId);
    }

    @Transactional(readOnly = true)
    public Page<AttractionResponse> getUserFavorites(Long userId, Pageable pageable) {
        // Validate user existence
        userService.findUserEntityById(userId);

        Page<UserFavorite> userFavoritesPage = userFavoriteRepository.findByUser_Id(userId, pageable);

        List<AttractionResponse> attractionResponses = userFavoritesPage.getContent().stream()
                .map(UserFavorite::getAttraction) // Get the Attraction entity from each UserFavorite
                .map(attractionService::convertToAttractionResponse) // Convert Attraction to AttractionResponse
                .collect(Collectors.toList());

        return new PageImpl<>(attractionResponses, pageable, userFavoritesPage.getTotalElements());
    }
}
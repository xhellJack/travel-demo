package com.example.travel.service;

import com.example.travel.dto.TagRequest;
import com.example.travel.dto.TagResponse;
import com.example.travel.entity.Tag;
import com.example.travel.exception.ConflictException;
import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    // --- DTO Converter ---
    public TagResponse convertToTagResponse(Tag tag) {
        if (tag == null) {
            return null;
        }
        TagResponse tagResponse = new TagResponse();
        tagResponse.setId(tag.getId());
        tagResponse.setName(tag.getName());
        tagResponse.setTagCategory(tag.getTagCategory());
        tagResponse.setDescription(tag.getDescription());
        return tagResponse;
    }
    public List<TagResponse> convertToTagResponseList(List<Tag> tags) {
        return tags.stream().map(this::convertToTagResponse).collect(Collectors.toList());
    }

    public Set<TagResponse> convertToTagResponseSet(Set<Tag> tags) {
        return tags.stream().map(this::convertToTagResponse).collect(Collectors.toSet());
    }


    // --- CRUD Operations ---

    @Transactional
    public TagResponse createTag(TagRequest tagRequest) {
        if (tagRepository.existsByName(tagRequest.getName())) {
            throw new ConflictException("Tag with name '" + tagRequest.getName() + "' already exists.");
        }
        Tag tag = new Tag();
        tag.setName(tagRequest.getName());
        tag.setTagCategory(tagRequest.getTagCategory());
        tag.setDescription(tagRequest.getDescription());

        Tag savedTag = tagRepository.save(tag);
        return convertToTagResponse(savedTag);
    }

    @Transactional(readOnly = true)
    public TagResponse getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        return convertToTagResponse(tag);
    }

    @Transactional(readOnly = true)
    public Tag findTagEntityById(Long id) { // Helper to get entity if needed by other services
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public TagResponse getTagByName(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with name: " + name));
        return convertToTagResponse(tag);
    }

    @Transactional(readOnly = true)
    public Tag findTagEntityByName(String name) { // Helper to get entity
        return tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with name: " + name));
    }


    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return convertToTagResponseList(tags);
    }

    @Transactional
    public TagResponse updateTag(Long id, TagRequest tagRequest) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        // Check if the new name conflicts with another existing tag
        if (tagRequest.getName() != null && !tagRequest.getName().equals(tag.getName())) {
            if (tagRepository.existsByNameAndIdNot(tagRequest.getName(), id)) {
                throw new ConflictException("Another tag with name '" + tagRequest.getName() + "' already exists.");
            }
            tag.setName(tagRequest.getName());
        }

        if (tagRequest.getTagCategory() != null) {
            tag.setTagCategory(tagRequest.getTagCategory());
        }
        if (tagRequest.getDescription() != null) {
            tag.setDescription(tagRequest.getDescription());
        }

        Tag updatedTag = tagRepository.save(tag);
        return convertToTagResponse(updatedTag);
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));


        tagRepository.delete(tag);
    }

    // Helper method to find or create tags, useful when associating tags by name
    @Transactional
    public Set<Tag> findOrCreateTagsByName(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName.trim())
                    .orElseGet(() -> {
                        // For simplicity, if a tag doesn't exist, create it with default/null category & description
                        // In a real app, you might want more control or throw an error if tags must pre-exist
                        Tag newTag = new Tag();
                        newTag.setName(tagName.trim());
                        // newTag.setTagCategory("Default"); // Optional: set a default category
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        return tags;
    }

    @Transactional(readOnly = true)
    public Set<Tag> findTagsByIds(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
        if (tags.size() != tagIds.size()) {
            // This means some tag IDs were not found.
            // You could log this or throw a more specific exception depending on requirements.
            System.out.println("Warning: Some tag IDs provided were not found in the database.");
        }
        return tags;
    }
}
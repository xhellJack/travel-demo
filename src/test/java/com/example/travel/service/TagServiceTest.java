package com.example.travel.service;

import com.example.travel.dto.TagRequest;
import com.example.travel.dto.TagResponse;
import com.example.travel.entity.Tag;
import com.example.travel.exception.ConflictException;
import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Use Mockito with JUnit 5
class TagServiceTest {

    @Mock // Creates a mock instance of TagRepository
    private TagRepository tagRepository;

    @InjectMocks // Creates an instance of TagService and injects the mocks into it
    private TagService tagService;

    private Tag tag1;
    private Tag tag2;
    private TagRequest tagRequest;

    @BeforeEach
    void setUp() {
        // Initialize common test objects
        tag1 = new Tag(1L, "历史古迹", "兴趣点", "包含重要历史事件或人物的地点", new HashSet<>(), new HashSet<>());
        tag2 = new Tag(2L, "自然风光", "兴趣点", "以自然景色为主的观光地", new HashSet<>(), new HashSet<>());

        tagRequest = new TagRequest();
        tagRequest.setName("美食");
        tagRequest.setTagCategory("餐饮风味");
        tagRequest.setDescription("特色餐饮");
    }

    @Test
    void convertToTagResponse_shouldReturnCorrectDTO() {
        TagResponse response = tagService.convertToTagResponse(tag1);
        assertNotNull(response);
        assertEquals(tag1.getId(), response.getId());
        assertEquals(tag1.getName(), response.getName());
        assertEquals(tag1.getTagCategory(), response.getTagCategory());
        assertEquals(tag1.getDescription(), response.getDescription());
    }

    @Test
    void convertToTagResponse_whenTagIsNull_shouldReturnNull() {
        TagResponse response = tagService.convertToTagResponse(null);
        assertNull(response);
    }

    @Test
    void convertToTagResponseList_shouldReturnCorrectDTOList() {
        List<Tag> tags = Arrays.asList(tag1, tag2);
        List<TagResponse> responses = tagService.convertToTagResponseList(tags);
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(tag1.getName(), responses.get(0).getName());
        assertEquals(tag2.getName(), responses.get(1).getName());
    }

    @Test
    void createTag_whenNameDoesNotExist_shouldCreateAndReturnTag() {
        Tag newTag = new Tag(null, tagRequest.getName(), tagRequest.getTagCategory(), tagRequest.getDescription(), new HashSet<>(), new HashSet<>());
        Tag savedTag = new Tag(3L, tagRequest.getName(), tagRequest.getTagCategory(), tagRequest.getDescription(), new HashSet<>(), new HashSet<>());

        when(tagRepository.existsByName(tagRequest.getName())).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag); // Mock save operation

        TagResponse createdTagResponse = tagService.createTag(tagRequest);

        assertNotNull(createdTagResponse);
        assertEquals(savedTag.getName(), createdTagResponse.getName());
        assertEquals(savedTag.getId(), createdTagResponse.getId());
        verify(tagRepository, times(1)).save(any(Tag.class)); // Verify save was called
    }

    @Test
    void createTag_whenNameExists_shouldThrowConflictException() {
        when(tagRepository.existsByName(tagRequest.getName())).thenReturn(true);

        assertThrows(ConflictException.class, () -> {
            tagService.createTag(tagRequest);
        });
        verify(tagRepository, never()).save(any(Tag.class)); // Verify save was NOT called
    }

    @Test
    void getTagById_whenTagExists_shouldReturnTagResponse() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag1));
        TagResponse response = tagService.getTagById(1L);
        assertNotNull(response);
        assertEquals(tag1.getName(), response.getName());
    }

    @Test
    void getTagById_whenTagDoesNotExist_shouldThrowResourceNotFoundException() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.getTagById(99L);
        });
    }

    @Test
    void getAllTags_shouldReturnListOfTagResponses() {
        List<Tag> tags = Arrays.asList(tag1, tag2);
        when(tagRepository.findAll()).thenReturn(tags);

        List<TagResponse> responses = tagService.getAllTags();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(tagRepository, times(1)).findAll();
    }

    @Test
    void updateTag_whenTagExistsAndNameNotConflicting_shouldUpdateAndReturnTag() {
        Long tagId = 1L;
        TagRequest updateRequest = new TagRequest("历史遗迹更新", "更新的分类", "更新的描述");
        Tag existingTag = new Tag(tagId, "历史古迹", "兴趣点", "描述", new HashSet<>(), new HashSet<>());
        Tag updatedTagEntity = new Tag(tagId, updateRequest.getName(), updateRequest.getTagCategory(), updateRequest.getDescription(), new HashSet<>(), new HashSet<>());

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(existingTag));
        when(tagRepository.existsByNameAndIdNot(updateRequest.getName(), tagId)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(updatedTagEntity);

        TagResponse updatedResponse = tagService.updateTag(tagId, updateRequest);

        assertNotNull(updatedResponse);
        assertEquals(updateRequest.getName(), updatedResponse.getName());
        assertEquals(updateRequest.getTagCategory(), updatedResponse.getTagCategory());
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void updateTag_whenTagNameConflicts_shouldThrowConflictException() {
        Long tagId = 1L;
        TagRequest updateRequest = new TagRequest("自然风光", "更新的分类", "更新的描述"); // Name conflicts with tag2
        Tag existingTag = new Tag(tagId, "历史古迹", "兴趣点", "描述", new HashSet<>(), new HashSet<>());

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(existingTag));
        when(tagRepository.existsByNameAndIdNot(updateRequest.getName(), tagId)).thenReturn(true); // Simulate name conflict

        assertThrows(ConflictException.class, () -> {
            tagService.updateTag(tagId, updateRequest);
        });
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_whenTagNotFound_shouldThrowResourceNotFoundException() {
        Long nonExistentTagId = 99L;
        TagRequest updateRequest = new TagRequest("Some Name", "Some Category", "Some Description");
        when(tagRepository.findById(nonExistentTagId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.updateTag(nonExistentTagId, updateRequest);
        });
        verify(tagRepository, never()).save(any(Tag.class));
    }


    @Test
    void deleteTag_whenTagExists_shouldCallDelete() {
        Long tagId = 1L;
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag1));
        // doNothing().when(tagRepository).delete(tag1); // For void methods

        tagService.deleteTag(tagId);

        verify(tagRepository, times(1)).delete(tag1);
    }

    @Test
    void deleteTag_whenTagNotFound_shouldThrowResourceNotFoundException() {
        Long nonExistentTagId = 99L;
        when(tagRepository.findById(nonExistentTagId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.deleteTag(nonExistentTagId);
        });
        verify(tagRepository, never()).delete(any(Tag.class));
    }

    @Test
    void findOrCreateTagsByName_whenTagsExist_shouldReturnExistingTags() {
        Set<String> tagNames = Set.of("历史古迹", "自然风光");
        when(tagRepository.findByName("历史古迹")).thenReturn(Optional.of(tag1));
        when(tagRepository.findByName("自然风光")).thenReturn(Optional.of(tag2));

        Set<Tag> foundTags = tagService.findOrCreateTagsByName(tagNames);

        assertEquals(2, foundTags.size());
        assertTrue(foundTags.contains(tag1));
        assertTrue(foundTags.contains(tag2));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void findOrCreateTagsByName_whenSomeTagsDoNotExist_shouldCreateNewTags() {
        Set<String> tagNames = Set.of("历史古迹", "新标签");
        Tag newTag = new Tag(null, "新标签", null, null, new HashSet<>(), new HashSet<>());
        Tag savedNewTag = new Tag(3L, "新标签", null, null, new HashSet<>(), new HashSet<>());

        when(tagRepository.findByName("历史古迹")).thenReturn(Optional.of(tag1));
        when(tagRepository.findByName("新标签")).thenReturn(Optional.empty()); // Does not exist initially
        when(tagRepository.save(any(Tag.class))).thenReturn(savedNewTag); // Mock saving of the new tag

        Set<Tag> foundTags = tagService.findOrCreateTagsByName(tagNames);

        assertEquals(2, foundTags.size());
        assertTrue(foundTags.contains(tag1));
        assertTrue(foundTags.stream().anyMatch(t -> t.getName().equals("新标签") && t.getId().equals(3L)));
        verify(tagRepository, times(1)).save(any(Tag.class)); // Verify one save call for "新标签"
    }

    @Test
    void findTagsByIds_shouldReturnCorrectTags() {
        Set<Long> tagIds = Set.of(1L, 2L);
        List<Tag> tagsFromRepo = Arrays.asList(tag1, tag2);
        when(tagRepository.findAllById(tagIds)).thenReturn(tagsFromRepo);

        Set<Tag> foundTags = tagService.findTagsByIds(tagIds);

        assertEquals(2, foundTags.size());
        assertTrue(foundTags.contains(tag1));
        assertTrue(foundTags.contains(tag2));
    }

    @Test
    void findTagsByIds_whenSomeIdsNotFound_shouldReturnFoundTagsAndLogWarning() {
        // This test would require verifying a log message, which can be complex with Mockito alone.
        // For now, we'll just check the returned set size.
        // The service method prints a warning to System.out.
        Set<Long> tagIds = Set.of(1L, 99L); // 99L does not exist
        List<Tag> tagsFromRepo = Arrays.asList(tag1); // Only tag1 is found
        when(tagRepository.findAllById(tagIds)).thenReturn(tagsFromRepo);

        Set<Tag> foundTags = tagService.findTagsByIds(tagIds);

        assertEquals(1, foundTags.size());
        assertTrue(foundTags.contains(tag1));
        // To verify the System.out warning, you'd typically use a Logback appender spy or similar logging test framework.
    }
}
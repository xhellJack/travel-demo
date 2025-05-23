package com.example.travel.service;

import com.example.travel.config.JwtTokenUtil;
import com.example.travel.dto.*;
import com.example.travel.entity.Tag;
import com.example.travel.entity.User;
import com.example.travel.exception.ConflictException;
import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.repository.TagRepository;
import com.example.travel.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TagRepository tagRepository; // 或者 TagService tagService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenUtil jwtTokenUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TagService tagService; // Mock TagService for DTO conversion


    @InjectMocks
    private UserService userService;

    private User user1;
    private UserRegistrationRequest registrationRequest;
    private UserUpdateRequest updateRequest;
    private Tag tag1;
    private TagResponse tagResponse1;


    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser");
        user1.setPassword("hashedPassword");
        user1.setEmail("test@example.com");
        user1.setFirstName("Test");
        user1.setLastName("User");
        user1.setActive(true);
        user1.getRoles().add("USER"); // Assuming roles are stored as simple strings

        tag1 = new Tag(1L, "历史", "兴趣点", "历史相关描述", new HashSet<>(), new HashSet<>());
        tagResponse1 = new TagResponse(1L, "历史", "兴趣点", "历史相关描述");


        registrationRequest = new UserRegistrationRequest(
                "newuser", "password123", "new@example.com", "New", "User"
        );

        updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFirstName("UpdatedFirst");
        updateRequest.setPreferredTagIds(Set.of(1L));



    }

    @Test
    void register_whenUsernameAndEmailAreNew_shouldSaveUserAndReturnUserResponse() {
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");

        // Mock the save operation
        // We need to be careful here to return a User entity that matches what save would do
        User savedUser = new User();
        savedUser.setId(2L); // Simulate a generated ID
        savedUser.setUsername(registrationRequest.getUsername());
        savedUser.setPassword("encodedPassword");
        savedUser.setEmail(registrationRequest.getEmail());
        savedUser.setFirstName(registrationRequest.getFirstName());
        savedUser.setLastName(registrationRequest.getLastName());
        savedUser.setActive(true);
        savedUser.getRoles().add("USER"); // Default role

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse userResponse = userService.register(registrationRequest);

        assertNotNull(userResponse);
        assertEquals(registrationRequest.getUsername(), userResponse.getUsername());
        assertEquals("USER", userResponse.getRoles().iterator().next()); // Check default role
        verify(passwordEncoder, times(1)).encode(registrationRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_whenUsernameExists_shouldThrowConflictException() {
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(registrationRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_whenCredentialsAreValid_shouldReturnAuthResponse() {
        UserLoginRequest loginRequest = new UserLoginRequest("testuser", "password123");
        Authentication authentication = mock(Authentication.class); // Mock the Authentication object
        UserDetails userDetails = mock(UserDetails.class); // Mock UserDetails

        when(userDetails.getUsername()).thenReturn("testuser");
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()))
        ).thenReturn(authentication); // Mock successful authentication

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user1));
        when(jwtTokenUtil.generateToken("testuser")).thenReturn("mocked.jwt.token");
        // Mock the tag conversion if user1 has preferredTags
        when(tagService.convertToTagResponseSet(user1.getPreferredTags())).thenReturn(new HashSet<>());


        AuthResponse authResponse = userService.login(loginRequest);

        assertNotNull(authResponse);
        assertEquals("mocked.jwt.token", authResponse.getToken());
        assertNotNull(authResponse.getUser());
        assertEquals("testuser", authResponse.getUser().getUsername());
    }

    @Test
    void login_whenCredentialsAreInvalid_shouldThrowException() {
        UserLoginRequest loginRequest = new UserLoginRequest("testuser", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.login(loginRequest));
    }

    @Test
    void getUserResponseById_whenUserExists_shouldReturnUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(tagService.convertToTagResponseSet(user1.getPreferredTags())).thenReturn(new HashSet<>()); // Assuming no preferred tags for simplicity here

        UserResponse response = userService.getUserResponseById(1L);

        assertNotNull(response);
        assertEquals(user1.getUsername(), response.getUsername());
    }

    @Test
    void getUserResponseById_whenUserNotExists_shouldThrowResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserResponseById(99L));
    }

    @Test
    void updateUser_whenUserExists_shouldUpdateAndReturnUserResponse() {
        Long userId = 1L;
        // updateRequest has preferredTagIds = Set.of(1L) from @BeforeEach
        // user1 is an existing user entity
        // tag1 is an existing tag entity (ID 1L)
        // tagResponse1 is the DTO for tag1

        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(userRepository.existsByEmailAndIdNot(updateRequest.getEmail(), userId)).thenReturn(false);

        // 1. Mock what tagService.findTagsByIds returns when called by userService.updateUser
        Set<Tag> actualTagsFetched = new HashSet<>();
        actualTagsFetched.add(tag1); // Assume tag with ID 1L is tag1
        when(tagService.findTagsByIds(eq(updateRequest.getPreferredTagIds()))).thenReturn(actualTagsFetched);

        // 2. Mock the userRepository.save(user)
        //    The 'user' object passed to save will have its 'preferredTags' field
        //    set to 'actualTagsFetched' by the updateUser method.
        //    We need to ensure that the 'updatedUser' object (which is the result of save)
        //    is the one passed to convertToUserResponse, AND that convertToUserResponse
        //    correctly uses tagService.convertToTagResponseSet on this 'actualTagsFetched'.
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        // Let save return the captured user, which should have its preferredTags updated.
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> userCaptor.getValue());



        // 3. Mock tagService.convertToTagResponseSet for the specific set of tags
        //    that will be on the 'updatedUser' object.
        //    This 'actualTagsFetched' is what 'updatedUser.getPreferredTags()' will return.
        Set<TagResponse> expectedTagResponseSet = new HashSet<>();
        if (!actualTagsFetched.isEmpty()) {
            expectedTagResponseSet.add(tagResponse1); // Ensure tagResponse1 is correctly defined
        }
        when(tagService.convertToTagResponseSet(eq(actualTagsFetched))).thenReturn(expectedTagResponseSet);


        // --- Execute the method under test ---
        UserResponse updatedResponse = userService.updateUser(userId, updateRequest);

        // --- Assertions ---
        assertNotNull(updatedResponse);
        assertEquals(updateRequest.getEmail(), updatedResponse.getEmail()); // Check other fields too

        assertNotNull(updatedResponse.getPreferredTags());
        if (!updateRequest.getPreferredTagIds().isEmpty()) {
            assertEquals(1, updatedResponse.getPreferredTags().size());
            TagResponse actualTagResponseInSet = updatedResponse.getPreferredTags().iterator().next();
            assertNotNull(actualTagResponseInSet); // This should not be null now
            assertEquals(tagResponse1.getName(), actualTagResponseInSet.getName());
        } else {
            assertTrue(updatedResponse.getPreferredTags().isEmpty());
        }

        // Verify save was called
        verify(userRepository).save(any(User.class));
        User savedUser = userCaptor.getValue();
        // Verify that the user passed to save had the correct preferred tags
        assertEquals(actualTagsFetched, savedUser.getPreferredTags());
    }


    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user1));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_whenUserNotExists_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("unknownuser"));
    }


    // ...更多测试用例 for deleteUser, getAllUsers etc.
}
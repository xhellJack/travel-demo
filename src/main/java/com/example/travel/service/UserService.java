package com.example.travel.service;

import com.example.travel.dto.*;
import com.example.travel.entity.Tag;
import com.example.travel.entity.User;

import com.example.travel.exception.ConflictException;
import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.repository.TagRepository;
import com.example.travel.repository.UserRepository;
import com.example.travel.config.JwtTokenUtil; // 您的JWT工具类

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.criteria.Predicate;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService,IUserService {

    private final UserRepository userRepository;
    // private final TagRepository tagRepository; // Can be removed if TagService handles all tag fetching
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    private AuthenticationManager authenticationManager;
    // private final RoleRepository roleRepository; // If using Role entity (we decided against this for now)
    private final TagService tagService; // Inject TagService
    private final TagRepository tagRepository; // Still needed for direct ID fetching

    @Autowired
    public void setAuthenticationManager(@Lazy AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenUtil jwtTokenUtil,
                       @Lazy AuthenticationManager authenticationManager,
                       // RoleRepository roleRepository,
                       TagService tagService, // Add TagService
                       TagRepository tagRepository) { // Keep TagRepository
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        // this.roleRepository = roleRepository;
        this.tagService = tagService; // Initialize TagService
        this.tagRepository = tagRepository; // Initialize TagRepository
    }

    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true); // 默认激活
        user.setEmailVerified(false); // 默认邮箱未验证，可以后续实现验证流程

        // 添加默认角色
        user.getRoles().add("USER"); // 或者 "ROLE_USER" 字符串

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    public AuthResponse login(UserLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        final String token = jwtTokenUtil.generateToken(userDetails.getUsername()); // 使用用户名生成Token

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after authentication"));

        return new AuthResponse(token, convertToUserResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserResponseById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public User findUserEntityById(Long id) { // Helper method if entity is needed internally or by other services
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }


    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // 检查邮箱是否更改，如果更改并且新邮箱已存在（且不属于当前用户），则抛出冲突异常
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new ConflictException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }

        // 更新偏好标签
        if (request.getPreferredTagIds() != null) {
            Set<Tag> preferredTags = new HashSet<>();
            if (!request.getPreferredTagIds().isEmpty()) {
                // Use TagService to fetch entities by ID, or keep using TagRepository directly
                preferredTags = tagService.findTagsByIds(request.getPreferredTagIds());
                // Or: preferredTags = new HashSet<>(tagRepository.findAllById(request.getPreferredTagIds()));
                // The TagService method might have better handling for not found IDs
            }
            user.setPreferredTags(preferredTags);
        }

        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable, String usernameFilter) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(usernameFilter)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + usernameFilter.toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(this::convertToUserResponse); // Use existing converter
    }

    @Transactional
    public UserResponse updateUserByAdmin(Long id, UserUpdateByAdminRequest request) {
        User user = findUserEntityById(id); // Existing method

        if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
                throw new ConflictException("Username already exists: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new ConflictException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getCountry() != null) user.setCountry(request.getCountry());
        if (request.getIsActive() != null) user.setActive(request.getIsActive()); // Correct setter
        if (request.getEmailVerified() != null) user.setEmailVerified(request.getEmailVerified());

        if (request.getRoles() != null) {
            // Assuming User.roles is Set<String> and roles in request are simple strings like "USER", "ADMIN"
            user.setRoles(new HashSet<>(request.getRoles()));
        }

        if (request.getPreferredTagIds() != null) {
            Set<Tag> preferredTags = new HashSet<>();
            if (!request.getPreferredTagIds().isEmpty()) {
                preferredTags = tagService.findTagsByIds(request.getPreferredTagIds());
            }
            user.setPreferredTags(preferredTags);
        }

        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(role -> "ROLE_" + role).toArray(String[]::new)) // Spring Security expects "ROLE_" prefix
                .accountExpired(!user.isActive()) // 示例逻辑
                .accountLocked(!user.isActive())  // 示例逻辑
                .credentialsExpired(!user.isActive()) // 示例逻辑
                .disabled(!user.isActive())       // 示例逻辑
                .build();
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Currently authenticated user not found in database"));
    }

    // --- Helper DTO Converters ---

    public boolean isSelf(Authentication authentication, Long userIdFromPath) {
        if (authentication == null || !authentication.isAuthenticated() || userIdFromPath == null) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String currentUsername = ((UserDetails) principal).getUsername();
            User currentUserEntity = userRepository.findByUsername(currentUsername).orElse(null);
            return currentUserEntity != null && currentUserEntity.getId().equals(userIdFromPath);
        }
        return false;
    }

    // Also, ensure these methods exist and return correct types in UserService:
    public User findUserEntityByUsername(String username) { // To get User entity for current user
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    public UserResponse convertToUserResponse(User user) {
        if (user == null) return null;
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setAvatar(user.getAvatar());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setGender(user.getGender());
        response.setCity(user.getCity());
        response.setCountry(user.getCountry());
        response.setIsActive(user.isActive());
        response.setEmailVerified(user.isEmailVerified());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setRoles(new HashSet<>(user.getRoles())); // 直接复制Set<String>

        if (user.getPreferredTags() != null && tagService != null) {
//            System.out.println("UserService.convertToUserResponse: Tags to convert: " + user.getPreferredTags());
            Set<TagResponse> convertedTags = tagService.convertToTagResponseSet(user.getPreferredTags());
//            System.out.println("UserService.convertToUserResponse: Converted tags: " + convertedTags);
            if (convertedTags != null) {
                convertedTags.forEach(tr -> System.out.println("Converted TagResponse element: " + tr));
            }
            response.setPreferredTags(convertedTags);
        } else {
            response.setPreferredTags(new HashSet<>());
        }
        return response;
    }


    @Override
    public User findByUsername(String username) {
        return null;
    }
}
package simply.Finsight_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simply.Finsight_backend.dto.request.UpdateUserRequest;
import simply.Finsight_backend.dto.response.UserResponse;
import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.enums.Role;
import simply.Finsight_backend.enums.UserStatus;
import simply.Finsight_backend.exception.BusinessException;
import simply.Finsight_backend.exception.DuplicateResourceException;
import simply.Finsight_backend.exception.ResourceNotFoundException;
import simply.Finsight_backend.mapper.UserMapper;
import simply.Finsight_backend.repository.UserRepository;
import simply.Finsight_backend.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users");
        Page<User> users = userRepository.findAll(pageable);
        return convertToResponsePage(users, pageable);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, UserStatus status) {
        log.info("Updating status for user ID: {} to {}", userId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getStatus() == status) {
            throw new BusinessException("User already has status: " + status);
        }

        user.setStatus(status);
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, Role role) {
        log.info("Updating role for user ID: {} to {}", userId, role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == role) {
            throw new BusinessException("User already has role: " + role);
        }

        user.setRole(role);
        return UserMapper.toResponse(user);
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        log.info("Searching users with keyword: {}", keyword);

        Page<User> users;
        if (keyword == null || keyword.isBlank()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.searchByNameOrEmail(keyword.trim(), pageable);
        }
        return convertToResponsePage(users, pageable);
    }

    @Override
    public Page<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable) {
        log.info("Filtering users by status: {}", status);
        Page<User> users = userRepository.findByStatus(status, pageable);
        return convertToResponsePage(users, pageable);
    }

    @Override
    public Page<UserResponse> getUsersByRole(Role role, Pageable pageable) {
        log.info("Filtering users by role: {}", role);
        Page<User> users = userRepository.findByRole(role, pageable);
        return convertToResponsePage(users, pageable);
    }

    @Override
    public UserResponse getCurrentUserProfile(String email) {
        log.info("Fetching profile for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(String email, UpdateUserRequest request) {
        log.info("Updating profile for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Account is " + user.getStatus() + ". Cannot update profile.");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            String newName = request.getName().trim();
            if (!newName.equals(user.getName())) {
                user.setName(newName);
            }
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new DuplicateResourceException("Email already in use: " + newEmail);
                }
                user.setEmail(newEmail);
            }
        }

        return UserMapper.toResponse(user);
    }

    private Page<UserResponse> convertToResponsePage(Page<User> page, Pageable pageable) {
        List<UserResponse> responseList = new ArrayList<>();
        for (User user : page.getContent()) {
            responseList.add(UserMapper.toResponse(user));
        }
        return new PageImpl<>(responseList, pageable, page.getTotalElements());
    }
}
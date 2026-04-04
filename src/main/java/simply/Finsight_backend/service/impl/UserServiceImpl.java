package simply.Finsight_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // ─── Get All Users ────────────────────────────────────────────

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users");
        return userRepository.findAll(pageable)
                .map(UserMapper::toResponse);
    }

    // ─── Get User By ID ───────────────────────────────────────────

    @Override
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
        return UserMapper.toResponse(user);
    }

    // ─── Update User Status ───────────────────────────────────────

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, UserStatus status) {
        log.info("Updating status for user ID: {} to {}", userId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        if (user.getStatus() == status) {
            throw new BusinessException(
                    "User already has status: " + status);
        }

        user.setStatus(status);
        // dirty checking saves automatically
        return UserMapper.toResponse(user);
    }

    // ─── Update User Role ─────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, Role role) {
        log.info("Updating role for user ID: {} to {}", userId, role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        if (user.getRole() == role) {
            throw new BusinessException(
                    "User already has role: " + role);
        }

        user.setRole(role);
        // dirty checking saves automatically
        return UserMapper.toResponse(user);
    }

    // ─── Search Users ─────────────────────────────────────────────

    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        log.info("Searching users with keyword: {}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return userRepository.findAll(pageable)
                    .map(UserMapper::toResponse);
        }
        return userRepository.searchByNameOrEmail(keyword.trim(), pageable)
                .map(UserMapper::toResponse);
    }

    // ─── Get Users By Status ──────────────────────────────────────

    @Override
    public Page<UserResponse> getUsersByStatus(UserStatus status,
                                               Pageable pageable) {
        log.info("Filtering users by status: {}", status);
        return userRepository.findByStatus(status, pageable)
                .map(UserMapper::toResponse);
    }

    // ─── Get Users By Role ────────────────────────────────────────

    @Override
    public Page<UserResponse> getUsersByRole(Role role, Pageable pageable) {
        log.info("Filtering users by role: {}", role);
        return userRepository.findByRole(role, pageable)
                .map(UserMapper::toResponse);
    }

    // ─── Get Current User Profile ─────────────────────────────────

    @Override
    public UserResponse getCurrentUserProfile(String email) {
        log.info("Fetching profile for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
        return UserMapper.toResponse(user);
    }

    // ─── Update Current User Profile ──────────────────────────────

    @Override
    @Transactional
    public UserResponse updateCurrentUser(String email,
                                          UpdateUserRequest request) {
        log.info("Updating profile for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));

        // block inactive users from updating profile
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(
                    "Account is " + user.getStatus()
                            + ". Cannot update profile.");
        }

        boolean isUpdated = false;

        // update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
            isUpdated = true;
        }

        // update email if provided and different
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new DuplicateResourceException(
                            "Email already in use: " + newEmail);
                }
                user.setEmail(newEmail);
                isUpdated = true;
            }
        }

        if (!isUpdated) {
            throw new BusinessException(
                    "No valid fields provided for update");
        }

        // dirty checking saves automatically
        return UserMapper.toResponse(user);
    }
}
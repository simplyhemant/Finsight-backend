package simply.Finsight_backend.service.impl;

import lombok.RequiredArgsConstructor;
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
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Requirement 1: Internal helper to verify if the requesting user is ACTIVE.
     */
    private void validateActiveStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Access Denied: Your account is " + user.getStatus() + ". Action restricted.");
        }
    }

    // --- Admin Operations (Usually triggered by an Admin, so we check Admin's status) ---

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, UserStatus status) {
        // We don't necessarily check the target user's status here,
        // because we are changing it. But the ADMIN performing this must be active.
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == role) {
            throw new BusinessException("User already has role: " + role);
        }

        user.setRole(role);
        return UserMapper.toResponse(user);
    }

    // --- Profile Operations ---

    @Override
    public UserResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        // Note: We allow users to see their profile even if INACTIVE
        // so they can see their status, but you can add validateActiveStatus(email)
        // here if you want total lockout.
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(String email, UpdateUserRequest request) {
        // Requirement 1: An INACTIVE user cannot update their own profile details.
        validateActiveStatus(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        boolean isUpdated = false;

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
            isUpdated = true;
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new DuplicateResourceException("Email already in use: " + newEmail);
                }
                user.setEmail(newEmail);
                isUpdated = true;
            }
        }

        if (!isUpdated) {
            throw new BusinessException("No valid fields provided for update");
        }

        return UserMapper.toResponse(user);
    }

    // --- Search & Filtering ---

    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        return (keyword == null || keyword.isBlank())
                ? userRepository.findAll(pageable).map(UserMapper::toResponse)
                : userRepository.searchByNameOrEmail(keyword.trim(), pageable).map(UserMapper::toResponse);
    }

    @Override
    public Page<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable).map(UserMapper::toResponse);
    }

    @Override
    public Page<UserResponse> getUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable).map(UserMapper::toResponse);
    }
}
package simply.Finsight_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import simply.Finsight_backend.dto.request.UpdateUserRequest;
import simply.Finsight_backend.dto.response.UserResponse;
import simply.Finsight_backend.enums.Role;
import simply.Finsight_backend.enums.UserStatus;

public interface UserService {

    // ─── Admin Operations ─────────
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long userId);
    UserResponse updateUserStatus(Long userId, UserStatus status);
    UserResponse updateUserRole(Long userId, Role role);
    Page<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable);
    Page<UserResponse> getUsersByRole(Role role, Pageable pageable);

    // ─── Search ──
    Page<UserResponse> searchUsers(String keyword, Pageable pageable);

    // ─── Profile ────────
    UserResponse getCurrentUserProfile(String email);
    UserResponse updateCurrentUser(String email, UpdateUserRequest request);
}
package simply.Finsight_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import simply.Finsight_backend.dto.request.UpdateUserRequest;
import simply.Finsight_backend.dto.request.UpdateUserRoleRequest;
import simply.Finsight_backend.dto.request.UpdateUserStatusRequest;
import simply.Finsight_backend.dto.response.ApiResponse;
import simply.Finsight_backend.dto.response.UserResponse;
import simply.Finsight_backend.service.UserService;
import simply.Finsight_backend.service.CustomUserDetails; // Correct principal import

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- Admin Operations (Requirement 1) ---

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        Sort sort = "asc".equalsIgnoreCase(order) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponse> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", users, HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", user, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        UserResponse response = userService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        UserResponse response = userService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", response, HttpStatus.OK.value()));
    }

    // --- Profile Operations (Stateless Authentication) ---

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Best Practice: Use the email from the secure principal object
        UserResponse user = userService.getCurrentUserProfile(currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", user, HttpStatus.OK.value()));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse user = userService.updateCurrentUser(currentUser.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user, HttpStatus.OK.value()));
    }

    // --- Search & Filtering (Optional Enhancement) ---

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search results fetched", users, HttpStatus.OK.value()));
    }
}
package simply.Finsight_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import simply.Finsight_backend.enums.Role;
import simply.Finsight_backend.enums.UserStatus;
import simply.Finsight_backend.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "User Management",
        description = "Admin-level user control and personal profile management")
public class UserController {

    private final UserService userService;

    // ---- admin operations ----
    @Operation(summary = "Get all users",
            description = "Paginated list of all users. ADMIN only.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "10")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String order) {

        log.info("Admin fetching all users. page={}, size={}", page, size);

        Sort sort = "asc".equalsIgnoreCase(order)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponse> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Users fetched successfully",
                users, HttpStatus.OK.value()));
    }

    @Operation(summary = "Get user by ID",
            description = "Single user details. ADMIN only.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id) {

        log.info("Admin fetching user ID: {}", id);
        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(ApiResponse.success(
                "User fetched successfully",
                user, HttpStatus.OK.value()));
    }


    @Operation(summary = "Update user status",
            description = "Activate or deactivate user. ADMIN only.")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        log.info("Updating status for user ID: {} to {}", id, request.getStatus());
        UserResponse response = userService.updateUserStatus(
                id, request.getStatus());

        return ResponseEntity.ok(ApiResponse.success(
                "User status updated successfully",
                response, HttpStatus.OK.value()));
    }

    @Operation(summary = "Update user role",
            description = "Change user role. ADMIN only.")
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        log.info("Updating role for user ID: {} to {}", id, request.getRole());
        UserResponse response = userService.updateUserRole(
                id, request.getRole());

        return ResponseEntity.ok(ApiResponse.success(
                "User role updated successfully",
                response, HttpStatus.OK.value()));
    }

    @Operation(summary = "Get users by status",
            description = "Filter users by ACTIVE/INACTIVE. ADMIN only.")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByStatus(
            @PathVariable UserStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching users with status: {}", status);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getUsersByStatus(
                status, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Users with status " + status + " fetched successfully",
                users, HttpStatus.OK.value()));
    }

    @Operation(summary = "Get users by role",
            description = "Filter users by role. ADMIN only.")
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByRole(
            @PathVariable Role role,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching users with role: {}", role);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getUsersByRole(role, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Users with role " + role + " fetched successfully",
                users, HttpStatus.OK.value()));
    }

   //  --- normal oepreation ---

    @Operation(summary = "Get own profile",
            description = "Get logged-in user's profile.")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal String email) {

        log.info("Fetching profile for: {}", email);
        UserResponse user = userService.getCurrentUserProfile(email);

        return ResponseEntity.ok(ApiResponse.success(
                "Profile fetched successfully",
                user, HttpStatus.OK.value()));
    }

    @Operation(summary = "Update own profile",
            description = "Update logged-in user's profile.")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating profile for: {}", email);
        UserResponse user = userService.updateCurrentUser(email, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Profile updated successfully",
                user, HttpStatus.OK.value()));
    }

    // --- admin

    @Operation(summary = "Search users",
            description = "Search by name or email keyword. ADMIN only.")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @Parameter(description = "Name or email keyword")
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Searching users with keyword: '{}'", keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.searchUsers(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Search results fetched successfully",
                users, HttpStatus.OK.value()));
    }
}
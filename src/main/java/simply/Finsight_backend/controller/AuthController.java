package simply.Finsight_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import simply.Finsight_backend.dto.request.ChangePasswordRequest;
import simply.Finsight_backend.dto.request.LoginRequest;
import simply.Finsight_backend.dto.request.RegisterRequest;
import simply.Finsight_backend.dto.response.ApiResponse;
import simply.Finsight_backend.dto.response.AuthResponse;
import simply.Finsight_backend.dto.response.UserResponse;
import simply.Finsight_backend.service.AuthService;
import simply.Finsight_backend.service.CustomUserDetails;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for User Registration, Login, and Profile Management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Creates a new account and returns authentication details.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User successfully registered")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Received registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);

        int status = HttpStatus.CREATED.value();
        log.info("User registered successfully: {}", request.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response, status));
    }

    @Operation(summary = "Login user", description = "Authenticates user credentials and returns a JWT token.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Received login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);

        int status = HttpStatus.OK.value();
        log.info("Login successful for email: {}", request.getEmail());

        return ResponseEntity
                .ok(ApiResponse.success("Login successful", response, status));
    }

    @Operation(summary = "Get current user profile", description = "Retrieves the profile details of the currently logged-in user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("Fetching profile for authenticated user: {}", currentUser.getUsername());
        UserResponse response = authService.getProfile(currentUser.getUsername());

        log.info("Profile fetched successfully for: {}", currentUser.getUsername());
        return ResponseEntity
                .ok(ApiResponse.success("Profile fetched successfully", response, HttpStatus.OK.value()));
    }

    @Operation(summary = "Change password", description = "Updates the password for the currently authenticated user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully")
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Password change initiated for user: {}", currentUser.getUsername());
        authService.changePassword(currentUser.getUsername(), request);

        log.info("Password changed successfully for user: {}", currentUser.getUsername());
        return ResponseEntity
                .ok(ApiResponse.success("Password changed successfully", null, HttpStatus.OK.value()));
    }

    @Operation(summary = "Logout user", description = "Handles user logout. Note: Client must discard the JWT token.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Logout request received");
        log.info("Logout process completed");
        return ResponseEntity
                .ok(ApiResponse.success("Logged out successfully", null, HttpStatus.OK.value()));
    }
}
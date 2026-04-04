package simply.Finsight_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import simply.Finsight_backend.service.CustomUserDetails; // Import your custom principal

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        int status = HttpStatus.CREATED.value();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response, status));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        int status = HttpStatus.OK.value();
        return ResponseEntity
                .ok(ApiResponse.success("Login successful", response, status));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Best Practice: Extract email from the principal object
        UserResponse response = authService.getProfile(currentUser.getUsername());
        return ResponseEntity
                .ok(ApiResponse.success("Profile fetched successfully", response, HttpStatus.OK.value()));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(currentUser.getUsername(), request);
        return ResponseEntity
                .ok(ApiResponse.success("Password changed successfully", null, HttpStatus.OK.value()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT logout is typically handled by client clearing the token
        return ResponseEntity
                .ok(ApiResponse.success("Logged out successfully", null, HttpStatus.OK.value()));
    }
}
package simply.Finsight_backend.service;

import simply.Finsight_backend.dto.request.ChangePasswordRequest;
import simply.Finsight_backend.dto.request.LoginRequest;
import simply.Finsight_backend.dto.request.RegisterRequest;
import simply.Finsight_backend.dto.response.AuthResponse;
import simply.Finsight_backend.dto.response.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);

    UserResponse getProfile(String email);

    void changePassword(String email, ChangePasswordRequest request);

    AuthResponse refreshToken(String refreshToken);
}
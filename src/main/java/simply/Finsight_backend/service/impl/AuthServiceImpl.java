package simply.Finsight_backend.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import simply.Finsight_backend.dto.request.ChangePasswordRequest;
import simply.Finsight_backend.dto.request.LoginRequest;
import simply.Finsight_backend.dto.request.RegisterRequest;
import simply.Finsight_backend.dto.response.AuthResponse;
import simply.Finsight_backend.dto.response.UserResponse;
import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.enums.Role;
import simply.Finsight_backend.enums.UserStatus;
import simply.Finsight_backend.exception.AuthenticationException;
import simply.Finsight_backend.exception.BusinessException;
import simply.Finsight_backend.exception.DuplicateResourceException;
import simply.Finsight_backend.exception.ResourceNotFoundException;
import simply.Finsight_backend.mapper.UserMapper;
import simply.Finsight_backend.repository.UserRepository;
import simply.Finsight_backend.security.JwtTokenProvider;
import simply.Finsight_backend.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.VIEWER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        String token = JwtTokenProvider.generateToken(
                user.getEmail(), user.getRole());

        return buildAuthResponse(token, user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException(
                        "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Account is deactivated. Contact admin.");
        }

        String token = JwtTokenProvider.generateToken(
                user.getEmail(), user.getRole());

        return buildAuthResponse(token, user);
    }

    @Override
    public void logout(String token) {
        // JWT is stateless — client drops the token
        if (token == null || token.isBlank()) {
            throw new AuthenticationException("Token is missing");
        }
    }

    @Override
    public UserResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(
                    "New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {

        if (!JwtTokenProvider.isTokenValid(refreshToken)) {
            throw new AuthenticationException("Invalid or expired token");
        }

        String email = JwtTokenProvider.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessException("Account is deactivated. Contact admin.");
        }

        String newToken = JwtTokenProvider.generateToken(
                user.getEmail(), user.getRole());

        return buildAuthResponse(newToken, user);
    }


    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .tokenType("Bearer")
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
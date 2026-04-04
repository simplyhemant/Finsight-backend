package simply.Finsight_backend.mapper;

import simply.Finsight_backend.dto.request.RegisterRequest;
import simply.Finsight_backend.dto.response.UserResponse;
import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.enums.Role;
import simply.Finsight_backend.enums.UserStatus;

public class UserMapper {

    // 🔹 Entity → DTO
    public static UserResponse toResponse(User user) {

        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // 🔹 RegisterRequest → Entity
    public static User toEntity(RegisterRequest request, String encodedPassword) {

        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(Role.VIEWER)   // default role
                .status(UserStatus.ACTIVE)   // default status
                .build();
    }
}
package simply.Finsight_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import simply.Finsight_backend.enums.Role;

@Data
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}
package simply.Finsight_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import simply.Finsight_backend.enums.UserStatus;

@Data
public class UpdateUserStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}
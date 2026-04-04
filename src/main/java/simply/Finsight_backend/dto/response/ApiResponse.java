package simply.Finsight_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Best practice: hides null fields on success
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    private String error;
    private int status; // Repopulated correctly for both success and error
    private String path;
    private Map<String, String> fieldErrors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Success Factory: Now includes status code
    public static <T> ApiResponse<T> success(String message, T data, int status) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(status)
                .build();
    }

    // Error Factory
    public static <T> ApiResponse<T> error(int status, String error, String message, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}
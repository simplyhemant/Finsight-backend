package simply.Finsight_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import simply.Finsight_backend.enums.TransactionType;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Type is required")
    private TransactionType type;
}
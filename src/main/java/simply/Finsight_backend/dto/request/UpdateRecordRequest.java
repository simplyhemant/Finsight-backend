package simply.Finsight_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import simply.Finsight_backend.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateRecordRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Type is required")
    private TransactionType type;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
package simply.Finsight_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import simply.Finsight_backend.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecordResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;

    private Long categoryId;
    private String categoryName;

    private LocalDate date;
    private String description;

    private Long createdById;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
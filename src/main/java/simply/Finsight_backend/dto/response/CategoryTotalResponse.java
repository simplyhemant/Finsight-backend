package simply.Finsight_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import simply.Finsight_backend.enums.TransactionType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTotalResponse {

    private String category;
    private BigDecimal total;
    private TransactionType type;
    private Long recordCount;
}
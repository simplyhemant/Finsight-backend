package simply.Finsight_backend.mapper;

import simply.Finsight_backend.dto.response.FinancialRecordResponse;
import simply.Finsight_backend.entity.FinancialRecord;

public class FinancialRecordMapper {

    public static FinancialRecordResponse toResponse(FinancialRecord record) {
        if (record == null) {
            return null;
        }

        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                
                // Safely get Category details (handles potential nulls even if DB says nullable=false)
                .categoryId(record.getCategory() != null ? record.getCategory().getId() : null)
                .categoryName(record.getCategory() != null ? record.getCategory().getName() : null)
                
                .date(record.getDate())
                .description(record.getDescription())
                
                // Safely get User ID (using your entity's 'createdBy' field)
                .createdById(record.getCreatedBy() != null ? record.getCreatedBy().getId() : null)
                
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
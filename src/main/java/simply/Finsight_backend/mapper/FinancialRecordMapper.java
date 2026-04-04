package simply.Finsight_backend.mapper;

import lombok.extern.slf4j.Slf4j;
import simply.Finsight_backend.dto.response.FinancialRecordResponse;
import simply.Finsight_backend.entity.FinancialRecord;

@Slf4j
public class FinancialRecordMapper {

    public static FinancialRecordResponse toResponse(FinancialRecord record) {
        // Handle Null input
        if (record == null) {
            log.warn("Attempted to map a null FinancialRecord entity");
            return null;
        }

        log.debug("Mapping FinancialRecord ID: {} to Response DTO", record.getId());

        FinancialRecordResponse response = new FinancialRecordResponse();

        // Map basic fields
        response.setId(record.getId());
        response.setAmount(record.getAmount());
        response.setType(record.getType());
        response.setDate(record.getDate());
        response.setDescription(record.getDescription());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());

        // Map Category details with if-else
        if (record.getCategory() != null) {
            response.setCategoryId(record.getCategory().getId());
            response.setCategoryName(record.getCategory().getName());
        } else {
            log.trace("Record ID {} has no associated category", record.getId());
            response.setCategoryId(null);
            response.setCategoryName(null);
        }

        // Map User (CreatedBy) details with if-else
        if (record.getCreatedBy() != null) {
            response.setCreatedById(record.getCreatedBy().getId());
        } else {
            log.trace("Record ID {} has no creator information", record.getId());
            response.setCreatedById(null);
        }

        return response;
    }
}
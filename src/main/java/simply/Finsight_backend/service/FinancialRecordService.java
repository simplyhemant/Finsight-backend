package simply.Finsight_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import simply.Finsight_backend.dto.request.CreateRecordRequest;
import simply.Finsight_backend.dto.request.UpdateRecordRequest;
import simply.Finsight_backend.dto.response.FinancialRecordResponse;
import simply.Finsight_backend.enums.TransactionType;

import java.time.LocalDate;

public interface FinancialRecordService {


    FinancialRecordResponse createRecord(String email, CreateRecordRequest request);

    FinancialRecordResponse getRecordById(Long id, String email);

    FinancialRecordResponse updateRecord(Long id, String email, UpdateRecordRequest request);

    void deleteRecord(Long id, String email);

    // ─── Admin Operations ──

    Page<FinancialRecordResponse> getAllRecords(Pageable pageable);

    // ─── User Specific Queries ────

    Page<FinancialRecordResponse> getRecordsByUser(String email,
            Pageable pageable);

    Page<FinancialRecordResponse> getRecordsByType(
            String email,
            TransactionType type,
            Pageable pageable);

    Page<FinancialRecordResponse> getRecordsByCategory(
            String email,
            Long categoryId,
            Pageable pageable);

    Page<FinancialRecordResponse> getRecordsByDateRange(
            String email,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);

    // ─── Advanced Filter ───

    Page<FinancialRecordResponse> filterRecords(
            String email,
            String keyword,
            TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);
}
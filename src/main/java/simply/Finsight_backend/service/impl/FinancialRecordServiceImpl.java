package simply.Finsight_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simply.Finsight_backend.dto.request.CreateRecordRequest;
import simply.Finsight_backend.dto.request.UpdateRecordRequest;
import simply.Finsight_backend.dto.response.FinancialRecordResponse;
import simply.Finsight_backend.entity.Category;
import simply.Finsight_backend.entity.FinancialRecord;
import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.enums.TransactionType;
import simply.Finsight_backend.enums.UserStatus;
import simply.Finsight_backend.exception.BusinessException;
import simply.Finsight_backend.exception.ResourceNotFoundException;
import simply.Finsight_backend.mapper.FinancialRecordMapper;
import simply.Finsight_backend.repository.CategoryRepository;
import simply.Finsight_backend.repository.FinancialRecordRepository;
import simply.Finsight_backend.repository.UserRepository;
import simply.Finsight_backend.service.FinancialRecordService;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // ─── Create Record (ADMIN only) ───────────────────────────────
    // @PreAuthorize on controller handles role check
    // No ownership logic needed — ADMIN manages all records

    private User validateAndGetActiveUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Access Denied: Your account is currently " + user.getStatus() +
                    ". You cannot perform financial operations.");
        }
        return user;
    }

    @Override
    @Transactional
    public FinancialRecordResponse createRecord(String email, CreateRecordRequest request) {
        User user = validateAndGetActiveUser(email);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        if (category.getType() != request.getType()) {
            throw new BusinessException(
                    "Category '" + category.getName() + "' is of type "
                            + category.getType() + " but record type is "
                            + request.getType());
        }

        FinancialRecord record = FinancialRecord.builder()
                .createdBy(user)
                .category(category)
                .amount(request.getAmount())
                .type(request.getType())
                .date(request.getDate())
                .description(request.getDescription())
                .deleted(false)
                .build();

        return FinancialRecordMapper.toResponse(recordRepository.save(record));
    }

    // ─── Get Record By ID (ANALYST + ADMIN) ───────────────────────
    // Both roles can view any record — no ownership check needed
    // @PreAuthorize on controller blocks VIEWER

    @Override
    public FinancialRecordResponse getRecordById(Long id, String email) {
        validateAndGetActiveUser(email);

        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Record not found with id: " + id));

        return FinancialRecordMapper.toResponse(record);
    }

    // ─── Update Record (ADMIN only) ───────────────────────────────
    // @PreAuthorize on controller handles role check
    // No ownership logic needed — ADMIN manages all records

    @Override
    @Transactional
    public FinancialRecordResponse updateRecord(Long id, String email, UpdateRecordRequest request) {
        validateAndGetActiveUser(email);

        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Record not found with id: " + id));

        Optional.ofNullable(request.getAmount()).ifPresent(record::setAmount);
        Optional.ofNullable(request.getType()).ifPresent(record::setType);
        Optional.ofNullable(request.getDate()).ifPresent(record::setDate);
        Optional.ofNullable(request.getDescription())
                .filter(d -> !d.isBlank())
                .ifPresent(record::setDescription);

        if (request.getCategoryId() != null
                && !request.getCategoryId().equals(record.getCategory().getId())) {

            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + request.getCategoryId()));

            TransactionType effectiveType = request.getType() != null ? request.getType() : record.getType();

            if (category.getType() != effectiveType) {
                throw new BusinessException("Category type does not match record type");
            }

            record.setCategory(category);
        }

        return FinancialRecordMapper.toResponse(record);
    }

    // ─── Delete Record (ADMIN only) ───────────────────────────────
    // @PreAuthorize on controller handles role check
    // Soft delete — sets deleted = true

    @Override
    @Transactional
    public void deleteRecord(Long id, String email) {
        validateAndGetActiveUser(email);

        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Record not found with id: " + id));

        record.setDeleted(true);
    }

    // ─── Get All Records (ADMIN only) ─────────────────────────────

    @Override
    public Page<FinancialRecordResponse> getAllRecords(Pageable pageable) {
        // Note: For system-wide logs, we don't necessarily check specific ownership,
        // but the security context typically provides the email of the Admin performing the action.
        return recordRepository.findAllByDeletedFalse(pageable)
                .map(FinancialRecordMapper::toResponse);
    }

    // ─── Get Records By User (ANALYST + ADMIN) ────────────────────
    // Returns records belonging to the logged-in user

    @Override
    public Page<FinancialRecordResponse> getRecordsByUser(String email, Pageable pageable) {
        User user = validateAndGetActiveUser(email);
        return recordRepository
                .findByCreatedBy_IdAndDeletedFalse(user.getId(), pageable)
                .map(FinancialRecordMapper::toResponse);
    }

    // ─── Get Records By Type (ANALYST + ADMIN) ────────────────────

    @Override
    public Page<FinancialRecordResponse> getRecordsByType(String email,
                                                          TransactionType type,
                                                          Pageable pageable) {
        return filterRecords(email, null, type, null, null, null, pageable);
    }

    // ─── Get Records By Category (ANALYST + ADMIN) ────────────────

    @Override
    public Page<FinancialRecordResponse> getRecordsByCategory(String email,
                                                              Long categoryId,
                                                              Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException(
                    "Category not found with id: " + categoryId);
        }
        return filterRecords(email, null, null, categoryId, null, null, pageable);
    }

    // ─── Get Records By Date Range (ANALYST + ADMIN) ──────────────

    @Override
    public Page<FinancialRecordResponse> getRecordsByDateRange(String email,
                                                               LocalDate startDate,
                                                               LocalDate endDate,
                                                               Pageable pageable) {
        return filterRecords(email, null, null, null, startDate, endDate, pageable);
    }

    // ─── Filter Records (ANALYST + ADMIN) ─────────────────────────
    // All other filter methods delegate here

    @Override
    public Page<FinancialRecordResponse> filterRecords(String email, String keyword, TransactionType type,
                                                       Long categoryId, LocalDate startDate, LocalDate endDate,
                                                       Pageable pageable) {
        User user = validateAndGetActiveUser(email);

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        return recordRepository.findWithUserFilters(
                        user.getId(), keyword, type, categoryId,
                        startDate, endDate, pageable)
                .map(FinancialRecordMapper::toResponse);
    }

}
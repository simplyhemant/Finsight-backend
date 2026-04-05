package simply.Finsight_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private User validateAndGetActiveUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Access Denied: Your account is " + user.getStatus());
        }
        return user;
    }

    @Override
    @Transactional
    public FinancialRecordResponse createRecord(String email, CreateRecordRequest request) {
        User user = validateAndGetActiveUser(email);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found ID: " + request.getCategoryId()));

        if (!category.isActive()) {
            throw new BusinessException("Category '" + category.getName() + "' is inactive.");
        }

        if (category.getType() != request.getType()) {
            throw new BusinessException("Record type does not match Category type.");
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

        FinancialRecord savedRecord = recordRepository.save(record);
        return FinancialRecordMapper.toResponse(savedRecord);
    }

    @Override
    public FinancialRecordResponse getRecordById(Long id, String email) {
        validateAndGetActiveUser(email);
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        return FinancialRecordMapper.toResponse(record);
    }

    @Override
    @Transactional
    public FinancialRecordResponse updateRecord(Long id, String email, UpdateRecordRequest request) {
        validateAndGetActiveUser(email);

        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));

        if (request.getAmount() != null) {
            record.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            record.setType(request.getType());
        }
        if (request.getDate() != null) {
            record.setDate(request.getDate());
        }
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            record.setDescription(request.getDescription());
        }

        if (request.getCategoryId() != null && !request.getCategoryId().equals(record.getCategory().getId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("New Category not found"));

            TransactionType currentType = (request.getType() != null) ? request.getType() : record.getType();

            if (category.getType() != currentType) {
                throw new BusinessException("New Category type does not match record type");
            }
            record.setCategory(category);
        }

        return FinancialRecordMapper.toResponse(record);
    }

    @Override
    @Transactional
    public void deleteRecord(Long id, String email) {
        validateAndGetActiveUser(email);
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));

        // Soft delete
        record.setDeleted(true);
    }

    @Override
    public Page<FinancialRecordResponse> getAllRecords(Pageable pageable) {
        Page<FinancialRecord> records = recordRepository.findAllByDeletedFalse(pageable);
        return convertToResponsePage(records, pageable);
    }

    @Override
    public Page<FinancialRecordResponse> getRecordsByUser(String email, Pageable pageable) {
        User user = validateAndGetActiveUser(email);
        Page<FinancialRecord> records = recordRepository.findByCreatedBy_IdAndDeletedFalse(user.getId(), pageable);
        return convertToResponsePage(records, pageable);
    }

    @Override
    public Page<FinancialRecordResponse> getRecordsByType(String email, TransactionType type, Pageable pageable) {
        return filterRecords(email, null, type, null, null, null, pageable);
    }

    @Override
    public Page<FinancialRecordResponse> getRecordsByCategory(String email, Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found");
        }
        return filterRecords(email, null, null, categoryId, null, null, pageable);
    }

    @Override
    public Page<FinancialRecordResponse> getRecordsByDateRange(String email, LocalDate start, LocalDate end, Pageable pageable) {
        return filterRecords(email, null, null, null, start, end, pageable);
    }

    @Override
    public Page<FinancialRecordResponse> filterRecords(String email, String keyword, TransactionType type,
                                                       Long categoryId, LocalDate startDate, LocalDate endDate,
                                                       Pageable pageable) {
        validateAndGetActiveUser(email);

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        Page<FinancialRecord> records = recordRepository.findWithFilters(keyword, type, categoryId, startDate, endDate, pageable);
        return convertToResponsePage(records, pageable);
    }

    private Page<FinancialRecordResponse> convertToResponsePage(Page<FinancialRecord> page, Pageable pageable) {
        List<FinancialRecordResponse> responseList = new ArrayList<>();

        // Traditional for-each loop
        for (FinancialRecord record : page.getContent()) {
            FinancialRecordResponse response = FinancialRecordMapper.toResponse(record);
            responseList.add(response);
        }

        return new PageImpl<>(responseList, pageable, page.getTotalElements());
    }
}
package simply.Finsight_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import simply.Finsight_backend.dto.request.CreateRecordRequest;
import simply.Finsight_backend.dto.request.UpdateRecordRequest;
import simply.Finsight_backend.dto.response.ApiResponse;
import simply.Finsight_backend.dto.response.FinancialRecordResponse;
import simply.Finsight_backend.enums.TransactionType;
import simply.Finsight_backend.service.FinancialRecordService;
import simply.Finsight_backend.service.CustomUserDetails;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Financial Records", description = "Endpoints for CRUD operations and filtering of financial transactions")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    @Operation(summary = "Create a record", description = "Allows an Admin to add a new income or expense record. Restricted to ROLE_ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Record created successfully")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> createRecord(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateRecordRequest request) {

        log.info("Admin [{}] is creating a new {} record of amount: {}",
                currentUser.getUsername(), request.getType(), request.getAmount());

        FinancialRecordResponse data = recordService.createRecord(currentUser.getUsername(), request);
        int status = HttpStatus.CREATED.value();

        log.info("Record created successfully with ID: {}", data.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Record created successfully", data, status));
    }

    @Operation(summary = "Get record by ID", description = "Fetch details of a specific transaction. Restricted to ROLE_ANALYST and ROLE_ADMIN.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> getRecordById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("User [{}] is fetching record ID: {}", currentUser.getUsername(), id);
        FinancialRecordResponse data = recordService.getRecordById(id, currentUser.getUsername());

        log.info("Record ID: {} retrieved successfully", id);
        return ResponseEntity.ok(
                ApiResponse.success("Record retrieved successfully", data, HttpStatus.OK.value()));
    }

    @Operation(summary = "Update a record", description = "Modify an existing record. Restricted to ROLE_ADMIN.")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> updateRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateRecordRequest request) {

        log.info("Admin [{}] is updating record ID: {}", currentUser.getUsername(), id);
        FinancialRecordResponse data = recordService.updateRecord(id, currentUser.getUsername(), request);

        log.info("Record ID: {} updated successfully", id);
        return ResponseEntity.ok(
                ApiResponse.success("Record updated successfully", data, HttpStatus.OK.value()));
    }

    @Operation(summary = "Delete a record", description = "Performs a soft delete on a transaction record. Restricted to ROLE_ADMIN.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("Admin [{}] is requesting deletion of record ID: {}", currentUser.getUsername(), id);
        recordService.deleteRecord(id, currentUser.getUsername());

        log.info("Record ID: {} deleted successfully (soft delete)", id);
        return ResponseEntity.ok(
                ApiResponse.success("Record deleted successfully", null, HttpStatus.OK.value()));
    }

    @Operation(summary = "List my records", description = "Retrieves a paginated list of records belonging to the current user.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getMyRecords(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(sort = "date", size = 20) Pageable pageable) {

        log.info("User [{}] is fetching their records. Page: {}, Size: {}",
                currentUser.getUsername(), pageable.getPageNumber(), pageable.getPageSize());

        Page<FinancialRecordResponse> data = recordService.getRecordsByUser(currentUser.getUsername(), pageable);

        log.info("Retrieved {} records for user: {}", data.getNumberOfElements(), currentUser.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Records retrieved successfully", data, HttpStatus.OK.value()));
    }

    @Operation(summary = "Filter records", description = "Search and filter records by keyword, type, category, or date range. Restricted to ROLE_ANALYST and ROLE_ADMIN.")
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> filterRecords(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Parameter(description = "Keyword to search in notes/category") @RequestParam(required = false) String keyword,
            @Parameter(description = "Type: INCOME or EXPENSE") @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Start Date (YYYY-MM-DD)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End Date (YYYY-MM-DD)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(sort = "date", size = 20) Pageable pageable) {

        log.info("User [{}] is applying filters - Keyword: {}, Type: {}, CategoryId: {}, Range: {} to {}",
                currentUser.getUsername(), keyword, type, categoryId, startDate, endDate);

        Page<FinancialRecordResponse> data = recordService.filterRecords(
                currentUser.getUsername(), keyword, type, categoryId,
                startDate, endDate, pageable);

        log.info("Filter applied. Found {} records", data.getTotalElements());
        return ResponseEntity.ok(
                ApiResponse.success("Filtered records retrieved successfully", data, HttpStatus.OK.value()));
    }

    @Operation(summary = "Admin: All System Records", description = "Audit log for Admins to view every record in the system. Restricted to ROLE_ADMIN.")
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getAllRecords(
            @PageableDefault(sort = "createdAt", size = 50) Pageable pageable) {

        log.info("Admin is fetching system-wide audit of all records. Page: {}", pageable.getPageNumber());
        Page<FinancialRecordResponse> data = recordService.getAllRecords(pageable);

        log.info("Total records in system: {}", data.getTotalElements());
        return ResponseEntity.ok(
                ApiResponse.success("All records retrieved successfully", data, HttpStatus.OK.value()));
    }
}
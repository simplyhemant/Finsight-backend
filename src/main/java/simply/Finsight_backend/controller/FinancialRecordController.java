package simply.Finsight_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import simply.Finsight_backend.service.CustomUserDetails; // Import your custom principal

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    // ─── POST /api/records ────────────────────────────────────────
    // ADMIN only — create record
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> createRecord(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateRecordRequest request) {

        FinancialRecordResponse data = recordService.createRecord(currentUser.getUsername(), request);
        int status = HttpStatus.CREATED.value();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Record created successfully", data, status));
    }

    // ─── GET /api/records/{id} ────────────────────────────────────
    // ANALYST + ADMIN — view any single record
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> getRecordById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        FinancialRecordResponse data = recordService.getRecordById(id, currentUser.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Record retrieved successfully", data, HttpStatus.OK.value()));
    }

    // ─── PATCH /api/records/{id} ──────────────────────────────────
    // ADMIN only — update record
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> updateRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateRecordRequest request) {

        FinancialRecordResponse data = recordService.updateRecord(id, currentUser.getUsername(), request);
        return ResponseEntity.ok(
                ApiResponse.success("Record updated successfully", data, HttpStatus.OK.value()));
    }

    // ─── DELETE /api/records/{id} ─────────────────────────────────
    // ADMIN only — soft delete record
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        recordService.deleteRecord(id, currentUser.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Record deleted successfully", null, HttpStatus.OK.value()));
    }

    // ─── GET /api/records ─────────────────────────────────────────
    // ANALYST + ADMIN — view own records
    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN', 'VIEWER')") // Allow Viewer as per assignment requirements
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getMyRecords(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(sort = "date", size = 20) Pageable pageable) {

        Page<FinancialRecordResponse> data = recordService.getRecordsByUser(currentUser.getUsername(), pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Records retrieved successfully", data, HttpStatus.OK.value()));
    }

    // ─── GET /api/records/filter ──────────────────────────────────
    // ANALYST + ADMIN — advanced filter (Insights)
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> filterRecords(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(sort = "date", size = 20) Pageable pageable) {

        Page<FinancialRecordResponse> data = recordService.filterRecords(
                currentUser.getUsername(), keyword, type, categoryId,
                startDate, endDate, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Filtered records retrieved successfully", data, HttpStatus.OK.value()));
    }

    // ─── GET /api/records/admin/all ───────────────────────────────
    // ADMIN only — view all records in system (Full Audit)
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getAllRecords(
            @PageableDefault(sort = "createdAt", size = 50) Pageable pageable) {

        Page<FinancialRecordResponse> data = recordService.getAllRecords(pageable);
        return ResponseEntity.ok(
                ApiResponse.success("All records retrieved successfully", data, HttpStatus.OK.value()));
    }
}
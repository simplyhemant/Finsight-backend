package simply.Finsight_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import simply.Finsight_backend.dto.request.CreateCategoryRequest;
import simply.Finsight_backend.dto.response.ApiResponse;
import simply.Finsight_backend.dto.response.CategoryResponse;
import simply.Finsight_backend.enums.TransactionType;
import simply.Finsight_backend.service.CategoryService;
import simply.Finsight_backend.service.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "Endpoints for managing financial categories (Income/Expense)")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category", description = "Allows an Admin to create a new transaction category. Restricted to ROLE_ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created successfully")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("Admin [{}] is creating a new category: {} for type: {}",
                currentUser.getUsername(), request.getName(), request.getType());

        CategoryResponse data = categoryService.createCategory(request, currentUser.getUserId());
        int status = HttpStatus.CREATED.value();

        log.info("Category created successfully with ID: {}", data.getId());
        return new ResponseEntity<>(
                ApiResponse.success("Category created successfully", data, status),
                HttpStatus.CREATED
        );
    }

    @Operation(summary = "Toggle category status", description = "Activate or Deactivate a category by ID. Restricted to ROLE_ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status toggled successfully")
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> toggle(@PathVariable Long id) {
        log.info("Toggle request received for category ID: {}", id);

        CategoryResponse data = categoryService.toggleCategoryStatus(id);
        String msg = "Category " + (data.isActive() ? "activated" : "deactivated") + " successfully";
        int status = HttpStatus.OK.value();

        log.info("Category [{}] status updated to active={}", id, data.isActive());
        return ResponseEntity.ok(ApiResponse.success(msg, data, status));
    }

    @Operation(summary = "Get category by ID", description = "Fetch details of a specific category.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category details retrieved")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        log.info("Fetching category details for ID: {}", id);

        CategoryResponse data = categoryService.getCategoryById(id);

        log.info("Category retrieved: {}", data.getName());
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", data, HttpStatus.OK.value()));
    }

    @Operation(summary = "Get all categories", description = "Retrieve every category in the system including inactive ones. Restricted to ROLE_ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Full list retrieved")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        log.info("Admin request to fetch all system categories");

        List<CategoryResponse> data = categoryService.getAllCategories();

        log.info("Total categories retrieved: {}", data.size());
        return ResponseEntity.ok(ApiResponse.success("All categories retrieved successfully", data, HttpStatus.OK.value()));
    }

    @Operation(summary = "Get all active categories", description = "Retrieve all categories that are currently active for use.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active list retrieved")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActive() {
        log.info("Fetching all active categories");

        List<CategoryResponse> data = categoryService.getAllActiveCategories();

        log.info("Total active categories retrieved: {}", data.size());
        return ResponseEntity.ok(ApiResponse.success("Active categories retrieved successfully", data, HttpStatus.OK.value()));
    }

    @Operation(summary = "Get active categories by type", description = "Filter active categories by transaction type (e.g., INCOME or EXPENSE).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Filtered list retrieved")
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getByType(
            @Parameter(description = "Type of transaction", example = "INCOME") @PathVariable TransactionType type) {
        log.info("Fetching active categories for type: {}", type);

        List<CategoryResponse> data = categoryService.getActiveCategoriesByType(type);

        log.info("Total categories retrieved for type [{}]: {}", type, data.size());
        return ResponseEntity.ok(ApiResponse.success("Categories for " + type + " retrieved successfully", data, HttpStatus.OK.value()));
    }
}
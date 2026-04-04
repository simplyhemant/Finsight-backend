package simply.Finsight_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Enforces RBAC
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        CategoryResponse data = categoryService.createCategory(request, currentUser.getUserId());
        int status = HttpStatus.CREATED.value();

        return new ResponseEntity<>(
                ApiResponse.success("Category created successfully", data, status),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> toggle(@PathVariable Long id) {
        CategoryResponse data = categoryService.toggleCategoryStatus(id);
        String msg = "Category " + (data.isActive() ? "activated" : "deactivated") + " successfully";
        int status = HttpStatus.OK.value();

        return ResponseEntity.ok(ApiResponse.success(msg, data, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        CategoryResponse data = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", data, HttpStatus.OK.value()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        List<CategoryResponse> data = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("All categories retrieved successfully", data, HttpStatus.OK.value()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActive() {
        List<CategoryResponse> data = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Active categories retrieved successfully", data, HttpStatus.OK.value()));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getByType(@PathVariable TransactionType type) {
        List<CategoryResponse> data = categoryService.getActiveCategoriesByType(type);
        return ResponseEntity.ok(ApiResponse.success("Categories for " + type + " retrieved successfully", data, HttpStatus.OK.value()));

    }
}
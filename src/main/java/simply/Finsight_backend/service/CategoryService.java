package simply.Finsight_backend.service;

import simply.Finsight_backend.dto.request.CreateCategoryRequest;
import simply.Finsight_backend.dto.response.CategoryResponse;
import simply.Finsight_backend.enums.TransactionType;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request, Long currentUserId);

    CategoryResponse toggleCategoryStatus(Long id);

    CategoryResponse getCategoryById(Long id);

    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getAllActiveCategories();

    List<CategoryResponse> getActiveCategoriesByType(TransactionType type);

}
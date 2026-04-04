package simply.Finsight_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simply.Finsight_backend.dto.request.CreateCategoryRequest;
import simply.Finsight_backend.dto.response.CategoryResponse;
import simply.Finsight_backend.entity.Category;
import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.enums.TransactionType;
import simply.Finsight_backend.enums.UserStatus; // Import your enum
import simply.Finsight_backend.exception.BusinessException;
import simply.Finsight_backend.exception.DuplicateResourceException;
import simply.Finsight_backend.exception.ResourceNotFoundException;
import simply.Finsight_backend.mapper.CategoryMapper;
import simply.Finsight_backend.repository.CategoryRepository;
import simply.Finsight_backend.repository.UserRepository;
import simply.Finsight_backend.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Requirement 1: Centralized status validation.
     * Ensures only ACTIVE users can perform category operations.
     */
    private User validateAndGetActiveUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Access Denied: Your account is " + user.getStatus() +
                    ". Inactive users cannot manage categories.");
        }
        return user;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request, Long currentUserId) {
        // 1. Validate User is Active
        User user = validateAndGetActiveUser();

        String cleanName = request.getName().trim();

        // 2. Duplicate Check
        if (categoryRepository.existsByNameIgnoreCase(cleanName)) {
            throw new DuplicateResourceException("Category already exists: " + cleanName);
        }

        // 3. Build the Entity
        Category category = Category.builder()
                .name(cleanName.toUpperCase())
                .type(request.getType())
                .active(true)
                .createdBy(user)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse toggleCategoryStatus(Long id) {
        // Ensure the person performing the toggle is Active
        validateAndGetActiveUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setActive(!category.isActive());

        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        validateAndGetActiveUser(); // Optional: remove if Viewers don't need active check
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        validateAndGetActiveUser();
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getAllActiveCategories() {
        validateAndGetActiveUser();
        return categoryRepository.findByActiveTrue().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getActiveCategoriesByType(TransactionType type) {
        validateAndGetActiveUser();
        return categoryRepository.findByTypeAndActiveTrue(type).stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }
}
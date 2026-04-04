package simply.Finsight_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simply.Finsight_backend.dto.request.CreateCategoryRequest;
import simply.Finsight_backend.dto.response.CategoryResponse;
import simply.Finsight_backend.entity.Category;
import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.enums.TransactionType;
import simply.Finsight_backend.enums.UserStatus;
import simply.Finsight_backend.exception.BusinessException;
import simply.Finsight_backend.exception.DuplicateResourceException;
import simply.Finsight_backend.exception.ResourceNotFoundException;
import simply.Finsight_backend.mapper.CategoryMapper;
import simply.Finsight_backend.repository.CategoryRepository;
import simply.Finsight_backend.repository.UserRepository;
import simply.Finsight_backend.service.CategoryService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request, Long currentUserId) {
        log.info("Attempting to create category: {}", request.getName());
        User user = validateAndGetActiveUser();

        String cleanName = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(cleanName)) {
            log.warn("Category creation failed: {} already exists", cleanName);
            throw new DuplicateResourceException("Category already exists: " + cleanName);
        }

        Category category = Category.builder()
                .name(cleanName.toUpperCase())
                .type(request.getType())
                .active(true)
                .createdBy(user)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Category '{}' created successfully with ID: {}", cleanName, savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse toggleCategoryStatus(Long id) {
        log.info("Toggling status for category ID: {}", id);
        validateAndGetActiveUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setActive(!category.isActive());
        log.info("Category ID: {} is now {}", id, category.isActive() ? "ACTIVE" : "INACTIVE");

        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        log.info("Fetching category by ID: {}", id);
        validateAndGetActiveUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories from database...");
        validateAndGetActiveUser();

        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> responseList = new ArrayList<>();

        // Traditional for-each loop
        for (Category category : categories) {
            responseList.add(categoryMapper.toResponse(category));
        }

        return responseList;
    }

    @Override
    public List<CategoryResponse> getAllActiveCategories() {
        log.info("Fetching all active categories...");
        validateAndGetActiveUser();

        List<Category> activeCategories = categoryRepository.findByActiveTrue();
        List<CategoryResponse> responseList = new ArrayList<>();

        for (Category category : activeCategories) {
            responseList.add(categoryMapper.toResponse(category));
        }

        return responseList;
    }

    @Override
    public List<CategoryResponse> getActiveCategoriesByType(TransactionType type) {
        log.info("Fetching active categories for type: {}", type);
        validateAndGetActiveUser();

        List<Category> categoriesByType = categoryRepository.findByTypeAndActiveTrue(type);
        List<CategoryResponse> responseList = new ArrayList<>();

        for (Category category : categoriesByType) {
            responseList.add(categoryMapper.toResponse(category));
        }

        return responseList;
    }

    private User validateAndGetActiveUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Blocked access attempt by INACTIVE user: {}", email);
            throw new BusinessException("Access Denied: Your account is " + user.getStatus() +
                    ". Inactive users cannot manage categories.");
        }

        return user;
    }
}
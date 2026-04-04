package simply.Finsight_backend.mapper;

import org.springframework.stereotype.Component;
import simply.Finsight_backend.dto.response.CategoryResponse;
import simply.Finsight_backend.entity.Category;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .active(category.isActive())
                // Access the ID from the User relationship
                .createdById(category.getCreatedBy() != null ? category.getCreatedBy().getId() : null)
                .build();
    }
}
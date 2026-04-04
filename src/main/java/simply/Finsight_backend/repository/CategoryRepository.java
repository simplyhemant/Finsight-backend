package simply.Finsight_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import simply.Finsight_backend.entity.Category;
import simply.Finsight_backend.enums.TransactionType;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.createdBy WHERE c.active = true")
    List<Category> findByActiveTrue();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.createdBy " +
            "WHERE c.type = :type AND c.active = true")
    List<Category> findByTypeAndActiveTrue(TransactionType type);


}
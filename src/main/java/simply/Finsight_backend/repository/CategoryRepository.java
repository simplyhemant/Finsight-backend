package simply.Finsight_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import simply.Finsight_backend.entity.Category;
import simply.Finsight_backend.enums.TransactionType;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ─── Finders ─────────────────────────────────────────────────

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // ─── Active Categories ────────────────────────────────────────

    List<Category> findByActiveTrue();

//    List<Category> findByActiveFalse();

    // ─── By Type ──────────────────────────────────────────────────

    List<Category> findByTypeAndActiveTrue(TransactionType type);

//    List<Category> findByType(TransactionType type);

    // ─── Active + Type Combined ───────────────────────────────────

//    boolean existsByNameIgnoreCaseAndType(String name, TransactionType type);
}
package simply.Finsight_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import simply.Finsight_backend.entity.FinancialRecord;
import simply.Finsight_backend.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // ─── Soft Delete Safe Finder ──────────────────────────────────
    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    // ─── Basic Filters ────────────────────────────────────────────
    Page<FinancialRecord> findAllByDeletedFalse(Pageable pageable);
    Page<FinancialRecord> findAllByDeletedTrue(Pageable pageable);
    Page<FinancialRecord> findByTypeAndDeletedFalse(
            TransactionType type, Pageable pageable);

    // ─── Main Filter (🔥 CORE QUERY) ──────────────────────────────
    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE r.deleted = false
            AND (:type     IS NULL OR r.type = :type)
            AND (:category IS NULL OR LOWER(r.category.name) = LOWER(:category))
            AND (:startDate IS NULL OR r.date >= :startDate)
            AND (:endDate   IS NULL OR r.date <= :endDate)
            """)
    Page<FinancialRecord> findWithFilters(
            @Param("type")      TransactionType type,
            @Param("category")  String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate,
            Pageable pageable);

    // ─── Dashboard Aggregations ───────────────────────────────────

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0)
            FROM FinancialRecord r
            WHERE r.deleted = false
            AND r.type = 'INCOME'
            """)
    BigDecimal getTotalIncome();

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0)
            FROM FinancialRecord r
            WHERE r.deleted = false
            AND r.type = 'EXPENSE'
            """)
    BigDecimal getTotalExpense();

    long countByDeletedFalse();

    // ─── Category Wise Totals ─────────────────────────────────────

    @Query("""
            SELECT r.category.name, r.type, SUM(r.amount), COUNT(r)
            FROM FinancialRecord r
            WHERE r.deleted = false
            GROUP BY r.category.name, r.type
            ORDER BY SUM(r.amount) DESC
            """)
    List<Object[]> getCategoryWiseTotals();

    // ─── Monthly Trends ───────────────────────────────────────────

    @Query("""
            SELECT YEAR(r.date), MONTH(r.date), r.type, SUM(r.amount)
            FROM FinancialRecord r
            WHERE r.deleted = false
            AND YEAR(r.date) = :year
            GROUP BY YEAR(r.date), MONTH(r.date), r.type
            ORDER BY MONTH(r.date) ASC
            """)
    List<Object[]> getMonthlyTrendsByYear(@Param("year") int year);

    // ─── Recent Activity ──────────────────────────────────────────

    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE r.deleted = false
            ORDER BY r.createdAt DESC
            """)
    List<FinancialRecord> findRecentActivity(Pageable pageable);

    // ─── Top Expense Categories ───────────────────────────────────

    @Query("""
            SELECT r.category.name, SUM(r.amount), COUNT(r)
            FROM FinancialRecord r
            WHERE r.deleted = false
            AND r.type = 'EXPENSE'
            GROUP BY r.category.name
            ORDER BY SUM(r.amount) DESC
            """)
    List<Object[]> getTopExpenseCategories(Pageable pageable);
}
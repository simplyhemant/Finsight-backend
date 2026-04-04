package simply.Finsight_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import simply.Finsight_backend.entity.FinancialRecord;
import simply.Finsight_backend.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long> {

    // ─── Core Finders ─────────────────────────────────────────────

    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    Page<FinancialRecord> findAllByDeletedFalse(Pageable pageable);

    Page<FinancialRecord> findByCreatedBy_IdAndDeletedFalse(
            Long userId, Pageable pageable);

    // ─── Master Filter ────────────────────────────────────────────

    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE r.createdBy.id = :userId
            AND r.deleted = false
            AND (CAST(:type AS string) IS NULL OR r.type = :type)
            AND (CAST(:categoryId AS long) IS NULL OR r.category.id = :categoryId)
            AND (CAST(:startDate AS localdate) IS NULL OR r.date >= :startDate)
            AND (CAST(:endDate AS localdate) IS NULL OR r.date <= :endDate)
            AND (CAST(:keyword AS string) IS NULL OR LOWER(r.description)
                 LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
            """)
    Page<FinancialRecord> findWithUserFilters(
            @Param("userId")     Long userId,
            @Param("keyword")    String keyword,
            @Param("type")       TransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate,
            Pageable pageable);

    // ─── Aggregations ─────────────────────────────────────────────

    @Query("SELECT SUM(r.amount) FROM FinancialRecord r " +
            "WHERE r.deleted = false AND r.type = 'INCOME'")
    BigDecimal getTotalIncome();

    @Query("SELECT SUM(r.amount) FROM FinancialRecord r " +
            "WHERE r.deleted = false AND r.type = 'EXPENSE'")
    BigDecimal getTotalExpense();

    // FIXED — param names match service calls (startDate/endDate)
    @Query(value = """
    SELECT COALESCE(SUM(amount), 0)
    FROM financial_records
    WHERE deleted = false
    AND type = :type
    AND (CAST(:startDate AS date) IS NULL OR date >= CAST(:startDate AS date))
    AND (CAST(:endDate AS date) IS NULL OR date <= CAST(:endDate AS date))
    """, nativeQuery = true)
    BigDecimal getTotalByTypeAndDateRange(
            @Param("type")      String type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate);

    long countByDeletedFalse();

    long countByTypeAndDeletedFalse(TransactionType type);

    // ─── Category Wise Totals ─────────────────────────────────────

    @Query("""
            SELECT r.category.name, r.type, SUM(r.amount), COUNT(r)
            FROM FinancialRecord r
            WHERE r.deleted = false
            GROUP BY r.category.name, r.type
            ORDER BY SUM(r.amount) DESC
            """)
    List<Object[]> getCategoryWiseTotals();

    // ─── Monthly Trends (JPQL + EXTRACT — PostgreSQL compatible) ──

    @Query("""
            SELECT EXTRACT(YEAR FROM r.date),
                   EXTRACT(MONTH FROM r.date),
                   r.type,
                   SUM(r.amount)
            FROM FinancialRecord r
            WHERE r.deleted = false
            AND EXTRACT(YEAR FROM r.date) = :year
            GROUP BY EXTRACT(YEAR FROM r.date),
                     EXTRACT(MONTH FROM r.date),
                     r.type
            ORDER BY EXTRACT(MONTH FROM r.date) ASC
            """)
    List<Object[]> getMonthlyTrendsByYear(@Param("year") int year);

    // ─── Weekly Trends (nativeQuery — EXTRACT WEEK is PostgreSQL only) ──

    @Query(value = """
            SELECT EXTRACT(YEAR FROM r.date)  AS year,
                   EXTRACT(WEEK FROM r.date)  AS week,
                   r.type                     AS type,
                   SUM(r.amount)              AS total
            FROM financial_records r
            WHERE r.deleted = false
            AND EXTRACT(YEAR FROM r.date) = :year
            GROUP BY EXTRACT(YEAR FROM r.date),
                     EXTRACT(WEEK FROM r.date),
                     r.type
            ORDER BY EXTRACT(WEEK FROM r.date) ASC
            """, nativeQuery = true)
    List<Object[]> getWeeklyTrendsByYear(@Param("year") int year);

    // ─── Top Categories ───────────────────────────────────────────

    @Query("""
            SELECT r.category.name, SUM(r.amount), COUNT(r)
            FROM FinancialRecord r
            WHERE r.deleted = false AND r.type = 'EXPENSE'
            GROUP BY r.category.name
            ORDER BY SUM(r.amount) DESC
            """)
    List<Object[]> getTopExpenseCategories(Pageable pageable);

    @Query("""
            SELECT r.category.name, SUM(r.amount), COUNT(r)
            FROM FinancialRecord r
            WHERE r.deleted = false AND r.type = 'INCOME'
            GROUP BY r.category.name
            ORDER BY SUM(r.amount) DESC
            """)
    List<Object[]> getTopIncomeCategories(Pageable pageable);

    // ─── Recent Activity ──────────────────────────────────────────

    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE r.deleted = false
            ORDER BY r.date DESC, r.createdAt DESC
            """)
    List<FinancialRecord> findRecentActivity(Pageable pageable);
}
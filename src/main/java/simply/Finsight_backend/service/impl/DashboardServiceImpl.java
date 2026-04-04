package simply.Finsight_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simply.Finsight_backend.dto.response.*;
import simply.Finsight_backend.entity.FinancialRecord;
import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.enums.TransactionType;
import simply.Finsight_backend.enums.UserStatus; // Import your enum
import simply.Finsight_backend.exception.BusinessException;
import simply.Finsight_backend.repository.FinancialRecordRepository;
import simply.Finsight_backend.repository.UserRepository;
import simply.Finsight_backend.service.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    /**
     * Requirement 1 & 4: Validates that the user exists and is ACTIVE.
     * Uses your custom UserStatus enum.
     */
    private void validateUserStatus() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Authentication Error: User record not found."));

        // Check against your UserStatus Enum
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Access Denied: Your account status is '" + user.getStatus() +
                    "'. Please contact an administrator to reactivate your account.");
        }
    }

    // ─── Get Summary ──────────────────────────────────────────────

    @Override
    public DashboardSummaryResponse getSummary() {
        validateUserStatus();

        BigDecimal totalIncome  = nullSafe(recordRepository.getTotalIncome());
        BigDecimal totalExpense = nullSafe(recordRepository.getTotalExpense());

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(totalIncome.subtract(totalExpense))
                .totalRecords(recordRepository.countByDeletedFalse())
                .totalIncomeRecords(recordRepository.countByTypeAndDeletedFalse(TransactionType.INCOME))
                .totalExpenseRecords(recordRepository.countByTypeAndDeletedFalse(TransactionType.EXPENSE))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    // ─── Get Category Wise Totals ─────────────────────────────────

    @Override
    public List<CategoryTotalResponse> getCategoryWiseTotals() {
        validateUserStatus();
        return mapToCategoryTotalResponse(recordRepository.getCategoryWiseTotals());
    }

    @Override
    public List<CategoryTotalResponse> getCategoryWiseTotalsByType(TransactionType type) {
        validateUserStatus();
        return getCategoryWiseTotals().stream()
                .filter(c -> c.getType() == type)
                .toList();
    }

    // ─── Get Monthly Trends ───────────────────────────────────────

    @Override
    public List<MonthlyTrendResponse> getMonthlyTrends(int year) {
        validateUserStatus();
        List<Object[]> results = recordRepository.getMonthlyTrendsByYear(year);
        Map<Integer, MonthlyTrendResponse> monthMap = new HashMap<>();

        for (Object[] row : results) {
            int monthNum = ((Number) row[1]).intValue();
            TransactionType type = TransactionType.valueOf(row[2].toString());
            BigDecimal amount    = nullSafe((BigDecimal) row[3]);

            MonthlyTrendResponse trend = monthMap.getOrDefault(monthNum,
                    MonthlyTrendResponse.builder()
                            .year(year).month(monthNum).monthName(Month.of(monthNum).name())
                            .totalIncome(BigDecimal.ZERO).totalExpense(BigDecimal.ZERO)
                            .netBalance(BigDecimal.ZERO).build());

            if (type == TransactionType.INCOME) trend.setTotalIncome(amount);
            else trend.setTotalExpense(amount);

            trend.setNetBalance(trend.getTotalIncome().subtract(trend.getTotalExpense()));
            monthMap.put(monthNum, trend);
        }

        return monthMap.values().stream()
                .sorted((a, b) -> Integer.compare(a.getMonth(), b.getMonth()))
                .toList();
    }

    // ─── Get Weekly Trends ────────────────────────────────────────

    @Override
    public List<WeeklyTrendResponse> getWeeklyTrends(int year) {
        validateUserStatus();
        List<Object[]> results = recordRepository.getWeeklyTrendsByYear(year);
        Map<Integer, WeeklyTrendResponse> weekMap = new HashMap<>();

        for (Object[] row : results) {
            int weekNum = ((Number) row[1]).intValue();
            TransactionType type = TransactionType.valueOf(row[2].toString());
            BigDecimal amount    = nullSafe((BigDecimal) row[3]);

            WeeklyTrendResponse trend = weekMap.getOrDefault(weekNum,
                    WeeklyTrendResponse.builder()
                            .year(year).week(weekNum).totalIncome(BigDecimal.ZERO)
                            .totalExpense(BigDecimal.ZERO).netBalance(BigDecimal.ZERO).build());

            if (type == TransactionType.INCOME) trend.setTotalIncome(amount);
            else trend.setTotalExpense(amount);

            trend.setNetBalance(trend.getTotalIncome().subtract(trend.getTotalExpense()));
            weekMap.put(weekNum, trend);
        }

        return weekMap.values().stream()
                .sorted((a, b) -> Integer.compare(a.getWeek(), b.getWeek()))
                .toList();
    }

    // ─── Get Recent Activity ──────────────────────────────────────

    @Override
    public List<RecentActivityResponse> getRecentActivity(int limit) {
        validateUserStatus();
        int safeLimit = Math.min(limit, 50);
        List<FinancialRecord> records = recordRepository.findRecentActivity(PageRequest.of(0, safeLimit));

        return records.stream()
                .map(r -> RecentActivityResponse.builder()
                        .id(r.getId()).amount(r.getAmount()).type(r.getType())
                        .category(r.getCategory() != null ? r.getCategory().getName() : null)
                        .date(r.getDate()).description(r.getDescription())
                        .createdByName(r.getCreatedBy() != null ? r.getCreatedBy().getName() : null)
                        .build())
                .toList();
    }

    // ─── Get Top Categories ───────────────────────────────────────

    @Override
    public List<CategoryTotalResponse> getTopExpenseCategories(int limit) {
        validateUserStatus();
        int safeLimit = Math.min(limit, 20);
        return mapToTopCategoryResponse(recordRepository.getTopExpenseCategories(PageRequest.of(0, safeLimit)), TransactionType.EXPENSE);
    }

    @Override
    public List<CategoryTotalResponse> getTopIncomeCategories(int limit) {
        validateUserStatus();
        int safeLimit = Math.min(limit, 20);
        return mapToTopCategoryResponse(recordRepository.getTopIncomeCategories(PageRequest.of(0, safeLimit)), TransactionType.INCOME);
    }

    // ─── Get Summary By Date Range ────────────────────────────────

    @Override
    public DashboardSummaryResponse getSummaryByDateRange(LocalDate startDate, LocalDate endDate) {
        validateUserStatus();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        BigDecimal totalIncome = nullSafe(recordRepository.getTotalByTypeAndDateRange(TransactionType.INCOME.name(), startDate, endDate));
        BigDecimal totalExpense = nullSafe(recordRepository.getTotalByTypeAndDateRange(TransactionType.EXPENSE.name(), startDate, endDate));

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome).totalExpense(totalExpense)
                .netBalance(totalIncome.subtract(totalExpense))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    // ─── Private Helpers ──────────────────────────────────────────

    private List<CategoryTotalResponse> mapToCategoryTotalResponse(List<Object[]> results) {
        List<CategoryTotalResponse> list = new ArrayList<>();
        for (Object[] row : results) {
            list.add(CategoryTotalResponse.builder()
                    .category((String) row[0])
                    .type(TransactionType.valueOf(row[1].toString()))
                    .total(nullSafe((BigDecimal) row[2]))
                    .recordCount(((Number) row[3]).longValue()).build());
        }
        return list;
    }

    private List<CategoryTotalResponse> mapToTopCategoryResponse(List<Object[]> results, TransactionType type) {
        List<CategoryTotalResponse> list = new ArrayList<>();
        for (Object[] row : results) {
            list.add(CategoryTotalResponse.builder()
                    .category((String) row[0]).type(type)
                    .total(nullSafe((BigDecimal) row[1]))
                    .recordCount(((Number) row[2]).longValue()).build());
        }
        return list;
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
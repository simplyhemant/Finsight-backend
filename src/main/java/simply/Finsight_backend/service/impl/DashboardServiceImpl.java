package simply.Finsight_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simply.Finsight_backend.dto.response.*;
import simply.Finsight_backend.entity.FinancialRecord;
import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.enums.TransactionType;
import simply.Finsight_backend.enums.UserStatus;
import simply.Finsight_backend.exception.BusinessException;
import simply.Finsight_backend.repository.FinancialRecordRepository;
import simply.Finsight_backend.repository.UserRepository;
import simply.Finsight_backend.service.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    // --- Get Summary ----

    @Override
    public DashboardSummaryResponse getSummary(Long userId) {
        log.info("Generating dashboard summary data for user: {}", userId);
        validateUserStatus();

        BigDecimal totalIncome = nullSafe(recordRepository.getTotalIncome());
        BigDecimal totalExpense = nullSafe(recordRepository.getTotalExpense());

        long countAll = recordRepository.countByDeletedFalse();
        long countIncome = recordRepository.countByTypeAndDeletedFalse(TransactionType.INCOME);
        long countExpense = recordRepository.countByTypeAndDeletedFalse(TransactionType.EXPENSE);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(totalIncome.subtract(totalExpense))
                .totalRecords(countAll)
                .totalIncomeRecords(countIncome)
                .totalExpenseRecords(countExpense)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    //---- Get Category Wise Totals ----

    @Override
    public List<CategoryTotalResponse> getCategoryWiseTotals(Long userId) {
        log.info("Fetching category-wise totals for user: {}", userId);
        validateUserStatus();
        return mapToCategoryTotalResponse(recordRepository.getCategoryWiseTotals());
    }

    @Override
    public List<CategoryTotalResponse> getCategoryWiseTotalsByType(TransactionType type, Long userId) {
        log.info("Filtering category totals by type: {} for user: {}", type, userId);
        validateUserStatus();

        List<CategoryTotalResponse> allTotals = getCategoryWiseTotals(userId);
        List<CategoryTotalResponse> filteredList = new ArrayList<>();

        // Simplified Loop instead of Stream
        for (CategoryTotalResponse item : allTotals) {
            if (item.getType() == type) {
                filteredList.add(item);
            }
        }
        return filteredList;
    }

    // Get Monthly Trends --
    @Override
    public List<MonthlyTrendResponse> getMonthlyTrends(int year, Long userId) {
        log.info("Calculating monthly trends for year: {} for user: {}", year, userId);
        validateUserStatus();

        List<Object[]> results = recordRepository.getMonthlyTrendsByYear(year);
        Map<Integer, MonthlyTrendResponse> monthMap = new HashMap<>();

        for (Object[] row : results) {
            int monthNum = ((Number) row[1]).intValue();
            TransactionType type = TransactionType.valueOf(row[2].toString());
            BigDecimal amount = nullSafe((BigDecimal) row[3]);

            MonthlyTrendResponse trend = monthMap.get(monthNum);
            if (trend == null) {
                trend = MonthlyTrendResponse.builder()
                        .year(year).month(monthNum).monthName(Month.of(monthNum).name())
                        .totalIncome(BigDecimal.ZERO).totalExpense(BigDecimal.ZERO)
                        .netBalance(BigDecimal.ZERO).build();
            }

            if (type == TransactionType.INCOME) {
                trend.setTotalIncome(amount);
            } else {
                trend.setTotalExpense(amount);
            }

            trend.setNetBalance(trend.getTotalIncome().subtract(trend.getTotalExpense()));
            monthMap.put(monthNum, trend);
        }

        List<MonthlyTrendResponse> sortedList = new ArrayList<>(monthMap.values());
        Collections.sort(sortedList, new Comparator<MonthlyTrendResponse>() {
            @Override
            public int compare(MonthlyTrendResponse a, MonthlyTrendResponse b) {
                return Integer.compare(a.getMonth(), b.getMonth());
            }
        });

        return sortedList;
    }

    // =-- Get Weekly Trends ----
    @Override
    public List<WeeklyTrendResponse> getWeeklyTrends(int year, Long userId) {
        log.info("Calculating weekly trends for year: {} for user: {}", year, userId);
        validateUserStatus();

        List<Object[]> results = recordRepository.getWeeklyTrendsByYear(year);
        Map<Integer, WeeklyTrendResponse> weekMap = new HashMap<>();

        for (Object[] row : results) {
            int weekNum = ((Number) row[1]).intValue();
            TransactionType type = TransactionType.valueOf(row[2].toString());
            BigDecimal amount = nullSafe((BigDecimal) row[3]);

            WeeklyTrendResponse trend = weekMap.get(weekNum);
            if (trend == null) {
                trend = WeeklyTrendResponse.builder()
                        .year(year).week(weekNum).totalIncome(BigDecimal.ZERO)
                        .totalExpense(BigDecimal.ZERO).netBalance(BigDecimal.ZERO).build();
            }

            if (type == TransactionType.INCOME) {
                trend.setTotalIncome(amount);
            } else {
                trend.setTotalExpense(amount);
            }

            trend.setNetBalance(trend.getTotalIncome().subtract(trend.getTotalExpense()));
            weekMap.put(weekNum, trend);
        }

        List<WeeklyTrendResponse> sortedList = new ArrayList<>(weekMap.values());
        Collections.sort(sortedList, new Comparator<WeeklyTrendResponse>() {
            @Override
            public int compare(WeeklyTrendResponse a, WeeklyTrendResponse b) {
                return Integer.compare(a.getWeek(), b.getWeek());
            }
        });

        return sortedList;
    }

    // --- Get Recent Activity -----

    @Override
    public List<RecentActivityResponse> getRecentActivity(int limit, Long userId) {
        log.info("Fetching {} most recent activity records for user: {}", limit, userId);
        validateUserStatus();

        int safeLimit = Math.min(limit, 50);
        List<FinancialRecord> records = recordRepository.findRecentActivity(PageRequest.of(0, safeLimit));
        List<RecentActivityResponse> responseList = new ArrayList<>();

        for (FinancialRecord r : records) {
            RecentActivityResponse dto = new RecentActivityResponse();
            dto.setId(r.getId());
            dto.setAmount(r.getAmount());
            dto.setType(r.getType());
            dto.setDate(r.getDate());
            dto.setDescription(r.getDescription());

            if (r.getCategory() != null) {
                dto.setCategory(r.getCategory().getName());
            }
            if (r.getCreatedBy() != null) {
                dto.setCreatedByName(r.getCreatedBy().getName());
            }

            responseList.add(dto);
        }
        return responseList;
    }

    // ---Top Categories ---

    @Override
    public List<CategoryTotalResponse> getTopExpenseCategories(int limit, Long userId) {
        log.info("Fetching top {} expense categories for user: {}", limit, userId);
        validateUserStatus();
        int safeLimit = Math.min(limit, 20);
        return mapToTopCategoryResponse(recordRepository.getTopExpenseCategories(PageRequest.of(0, safeLimit)), TransactionType.EXPENSE);
    }

    @Override
    public List<CategoryTotalResponse> getTopIncomeCategories(int limit, Long userId) {
        log.info("Fetching top {} income categories for user: {}", limit, userId);
        validateUserStatus();
        int safeLimit = Math.min(limit, 20);
        return mapToTopCategoryResponse(recordRepository.getTopIncomeCategories(PageRequest.of(0, safeLimit)), TransactionType.INCOME);
    }

    // --- Summary By Date Range

    @Override
    public DashboardSummaryResponse getSummaryByDateRange(LocalDate startDate, LocalDate endDate, Long userId) {
        log.info("Fetching summary for range: {} to {} for user: {}", startDate, endDate, userId);
        validateUserStatus();

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            log.error("Invalid date range requested: {} - {}", startDate, endDate);
            throw new BusinessException("Start date cannot be after end date");
        }

        BigDecimal income = nullSafe(recordRepository.getTotalByTypeAndDateRange(TransactionType.INCOME.name(), startDate, endDate));
        BigDecimal expense = nullSafe(recordRepository.getTotalByTypeAndDateRange(TransactionType.EXPENSE.name(), startDate, endDate));

        long countAll = recordRepository.countByDateRange(startDate, endDate);
        long countIncome = recordRepository.countByTypeAndDateRange(TransactionType.INCOME.name(), startDate, endDate);
        long countExpense = recordRepository.countByTypeAndDateRange(TransactionType.EXPENSE.name(), startDate, endDate);

        return DashboardSummaryResponse.builder()
                .totalIncome(income)
                .totalExpense(expense)
                .netBalance(income.subtract(expense))
                .totalRecords(countAll)
                .totalIncomeRecords(countIncome)
                .totalExpenseRecords(countExpense)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    // --- Private Helpers methods ----

    private void validateUserStatus() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Authentication Error: User record not found."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Access denied for inactive user: {}", email);
            throw new BusinessException("Access Denied: Your account status is '" + user.getStatus() +
                    "'. Please contact an administrator to reactivate your account.");
        }
    }

    private List<CategoryTotalResponse> mapToCategoryTotalResponse(List<Object[]> results) {
        List<CategoryTotalResponse> list = new ArrayList<>();
        for (Object[] row : results) {
            CategoryTotalResponse dto = new CategoryTotalResponse();
            dto.setCategory((String) row[0]);
            dto.setType(TransactionType.valueOf(row[1].toString()));
            dto.setTotal(nullSafe((BigDecimal) row[2]));
            dto.setRecordCount(((Number) row[3]).longValue());
            list.add(dto);
        }
        return list;
    }

    private List<CategoryTotalResponse> mapToTopCategoryResponse(List<Object[]> results, TransactionType type) {
        List<CategoryTotalResponse> list = new ArrayList<>();
        for (Object[] row : results) {
            CategoryTotalResponse dto = new CategoryTotalResponse();
            dto.setCategory((String) row[0]);
            dto.setType(type);
            dto.setTotal(nullSafe((BigDecimal) row[1]));
            dto.setRecordCount(((Number) row[2]).longValue());
            list.add(dto);
        }
        return list;
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
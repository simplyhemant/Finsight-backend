package simply.Finsight_backend.service;

import simply.Finsight_backend.dto.response.CategoryTotalResponse;
import simply.Finsight_backend.dto.response.DashboardSummaryResponse;
import simply.Finsight_backend.dto.response.MonthlyTrendResponse;
import simply.Finsight_backend.dto.response.RecentActivityResponse;
import simply.Finsight_backend.dto.response.WeeklyTrendResponse;
import simply.Finsight_backend.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardSummaryResponse getSummary(Long userId);

    // ─── Category Totals ───
    List<CategoryTotalResponse> getCategoryWiseTotals(Long userId);
    List<CategoryTotalResponse> getCategoryWiseTotalsByType(TransactionType type, Long userId);

    // ─── Trends ───
    List<MonthlyTrendResponse> getMonthlyTrends(int year, Long userId);
    List<WeeklyTrendResponse>  getWeeklyTrends(int year, Long userId);

    // ─── Recent Activity ──
    List<RecentActivityResponse> getRecentActivity(int limit, Long userId);

    // ─── Top Categories ───
    List<CategoryTotalResponse> getTopExpenseCategories(int limit, Long userId);
    List<CategoryTotalResponse> getTopIncomeCategories(int limit, Long userId);

    // ─── Date Range Summary ────
    DashboardSummaryResponse getSummaryByDateRange(LocalDate startDate,
                                                   LocalDate endDate,
                                                   Long userId);
}
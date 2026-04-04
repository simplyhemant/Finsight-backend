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

    DashboardSummaryResponse getSummary();

    // ─── Category Totals ──────────────────────────────────────────
    List<CategoryTotalResponse> getCategoryWiseTotals();
    List<CategoryTotalResponse> getCategoryWiseTotalsByType(TransactionType type);

    // ─── Trends ───────────────────────────────────────────────────
    List<MonthlyTrendResponse> getMonthlyTrends(int year);
    List<WeeklyTrendResponse>  getWeeklyTrends(int year);

    // ─── Recent Activity ──────────────────────────────────────────
    List<RecentActivityResponse> getRecentActivity(int limit);

    // ─── Top Categories ───────────────────────────────────────────
    List<CategoryTotalResponse> getTopExpenseCategories(int limit);
    List<CategoryTotalResponse> getTopIncomeCategories(int limit);

    // ─── Date Range Summary ───────────────────────────────────────
    DashboardSummaryResponse getSummaryByDateRange(LocalDate startDate,
                                                   LocalDate endDate);
}
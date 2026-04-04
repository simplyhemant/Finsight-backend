package simply.Finsight_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import simply.Finsight_backend.dto.response.ApiResponse;
import simply.Finsight_backend.dto.response.CategoryTotalResponse;
import simply.Finsight_backend.dto.response.DashboardSummaryResponse;
import simply.Finsight_backend.dto.response.MonthlyTrendResponse;
import simply.Finsight_backend.dto.response.RecentActivityResponse;
import simply.Finsight_backend.dto.response.WeeklyTrendResponse;
import simply.Finsight_backend.enums.TransactionType;
import simply.Finsight_backend.service.DashboardService;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
@Tag(name = "Dashboard & Analytics", description = "Endpoints for fetching aggregated financial data and trends")
public class DashboardController {

    private final DashboardService dashboardService;

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(
                ApiResponse.success(message, data, HttpStatus.OK.value()));
    }

    @Operation(summary = "Get overall summary", description = "Returns total income, total expense, and net balance.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Summary fetched successfully")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        log.info("Request received for dashboard summary");
        DashboardSummaryResponse data = dashboardService.getSummary();
        log.info("Dashboard summary fetched successfully");
        return ok("Dashboard summary retrieved successfully", data);
    }

    @Operation(summary = "Get category totals", description = "Returns total amounts grouped by category.")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryTotalResponse>>> getCategoryWiseTotals() {
        log.info("Request received for category-wise totals");
        List<CategoryTotalResponse> data = dashboardService.getCategoryWiseTotals();
        log.info("Retrieved {} category-wise totals", data.size());
        return ok("Category totals retrieved successfully", data);
    }

    @Operation(summary = "Get category totals by type", description = "Returns category totals filtered by INCOME or EXPENSE.")
    @GetMapping("/categories/{type}")
    public ResponseEntity<ApiResponse<List<CategoryTotalResponse>>> getCategoryWiseTotalsByType(
            @Parameter(description = "Transaction type to filter by") @PathVariable TransactionType type) {
        log.info("Request received for category totals by type: {}", type);
        List<CategoryTotalResponse> data = dashboardService.getCategoryWiseTotalsByType(type);
        log.info("Retrieved {} category totals for type: {}", data.size(), type);
        return ok("Category totals by type retrieved successfully", data);
    }

    @Operation(summary = "Get monthly trends", description = "Returns month-by-month financial data for a specific year.")
    @GetMapping("/trends/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyTrendResponse>>> getMonthlyTrends(
            @RequestParam(required = false) @Parameter(description = "Year for trends (defaults to current year)") Integer year) {
        int targetYear = (year != null) ? year : Year.now().getValue();
        log.info("Request received for monthly trends for year: {}", targetYear);
        List<MonthlyTrendResponse> data = dashboardService.getMonthlyTrends(targetYear);
        log.info("Retrieved {} monthly trend entries for year: {}", data.size(), targetYear);
        return ok("Monthly trends retrieved successfully", data);
    }

    @Operation(summary = "Get weekly trends", description = "Returns week-by-week financial data for a specific year.")
    @GetMapping("/trends/weekly")
    public ResponseEntity<ApiResponse<List<WeeklyTrendResponse>>> getWeeklyTrends(
            @RequestParam(required = false) @Parameter(description = "Year for trends (defaults to current year)") Integer year) {
        int targetYear = (year != null) ? year : Year.now().getValue();
        log.info("Request received for weekly trends for year: {}", targetYear);
        List<WeeklyTrendResponse> data = dashboardService.getWeeklyTrends(targetYear);
        log.info("Retrieved {} weekly trend entries for year: {}", data.size(), targetYear);
        return ok("Weekly trends retrieved successfully", data);
    }

    @Operation(summary = "Get recent activity", description = "Returns a list of the most recent transactions.")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<RecentActivityResponse>>> getRecentActivity(
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of records to fetch") int limit) {
        log.info("Request received for recent activity with limit: {}", limit);
        List<RecentActivityResponse> data = dashboardService.getRecentActivity(limit);
        log.info("Retrieved {} recent activity records", data.size());
        return ok("Recent activity retrieved successfully", data);
    }

    @Operation(summary = "Top expense categories", description = "Returns the categories with the highest spending.")
    @GetMapping("/top-categories/expense")
    public ResponseEntity<ApiResponse<List<CategoryTotalResponse>>> getTopExpenseCategories(
            @RequestParam(defaultValue = "5") @Parameter(description = "Number of categories to fetch") int limit) {
        log.info("Request received for top expense categories with limit: {}", limit);
        List<CategoryTotalResponse> data = dashboardService.getTopExpenseCategories(limit);
        log.info("Retrieved {} top expense categories", data.size());
        return ok("Top expense categories retrieved successfully", data);
    }

    @Operation(summary = "Top income categories", description = "Returns the categories with the highest earnings.")
    @GetMapping("/top-categories/income")
    public ResponseEntity<ApiResponse<List<CategoryTotalResponse>>> getTopIncomeCategories(
            @RequestParam(defaultValue = "5") @Parameter(description = "Number of categories to fetch") int limit) {
        log.info("Request received for top income categories with limit: {}", limit);
        List<CategoryTotalResponse> data = dashboardService.getTopIncomeCategories(limit);
        log.info("Retrieved {} top income categories", data.size());
        return ok("Top income categories retrieved successfully", data);
    }

    @Operation(summary = "Summary by date range", description = "Returns totals for a specific period of time.")
    @GetMapping("/summary/date-range")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummaryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate) {
        log.info("Request received for summary in date range: {} to {}", startDate, endDate);
        DashboardSummaryResponse data = dashboardService.getSummaryByDateRange(startDate, endDate);
        log.info("Summary for date range {} to {} fetched successfully", startDate, endDate);
        return ok("Date range summary retrieved successfully", data);
    }
}
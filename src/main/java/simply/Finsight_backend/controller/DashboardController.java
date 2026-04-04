package simply.Finsight_backend.controller;

import lombok.RequiredArgsConstructor;
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
@PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    // ─── helper to reduce boilerplate ────────────────────────────
    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(
                ApiResponse.success(message, data, HttpStatus.OK.value()));
    }

    // ─── GET /api/dashboard/summary ───────────────────────────────
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ok("Dashboard summary retrieved successfully",
                dashboardService.getSummary());
    }

    // ─── GET /api/dashboard/categories ───────────────────────────
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryTotalResponse>>> getCategoryWiseTotals() {
        return ok("Category totals retrieved successfully",
                dashboardService.getCategoryWiseTotals());
    }

    // ─── GET /api/dashboard/categories/{type} ─────────────────────
    @GetMapping("/categories/{type}")
    public ResponseEntity<ApiResponse<List<CategoryTotalResponse>>> getCategoryWiseTotalsByType(
            @PathVariable TransactionType type) {
        return ok("Category totals by type retrieved successfully",
                dashboardService.getCategoryWiseTotalsByType(type));
    }

    // ─── GET /api/dashboard/trends/monthly?year=2025 ──────────────
    @GetMapping("/trends/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyTrendResponse>>> getMonthlyTrends(
            @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : Year.now().getValue();
        return ok("Monthly trends retrieved successfully",
                dashboardService.getMonthlyTrends(targetYear));
    }

    // ─── GET /api/dashboard/trends/weekly?year=2025 ───────────────
    @GetMapping("/trends/weekly")
    public ResponseEntity<ApiResponse<List<WeeklyTrendResponse>>> getWeeklyTrends(
            @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : Year.now().getValue();
        return ok("Weekly trends retrieved successfully",
                dashboardService.getWeeklyTrends(targetYear));
    }

    // ─── GET /api/dashboard/recent?limit=10 ───────────────────────
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<RecentActivityResponse>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        return ok("Recent activity retrieved successfully",
                dashboardService.getRecentActivity(limit));
    }

    // ─── GET /api/dashboard/top-categories/expense?limit=5 ────────
    @GetMapping("/top-categories/expense")
    public ResponseEntity<ApiResponse<List<CategoryTotalResponse>>> getTopExpenseCategories(
            @RequestParam(defaultValue = "5") int limit) {
        return ok("Top expense categories retrieved successfully",
                dashboardService.getTopExpenseCategories(limit));
    }

    // ─── GET /api/dashboard/top-categories/income?limit=5 ─────────
    @GetMapping("/top-categories/income")
    public ResponseEntity<ApiResponse<List<CategoryTotalResponse>>> getTopIncomeCategories(
            @RequestParam(defaultValue = "5") int limit) {
        return ok("Top income categories retrieved successfully",
                dashboardService.getTopIncomeCategories(limit));
    }

    // ─── GET /api/dashboard/summary/date-range ────────────────────
    @GetMapping("/summary/date-range")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummaryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {
        return ok("Date range summary retrieved successfully",
                dashboardService.getSummaryByDateRange(startDate, endDate));
    }
}
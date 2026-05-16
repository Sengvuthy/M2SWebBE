package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.DTO.ExpenseIdSummaryDTO;
import SuperiorPro.SuperiorPOS.DTO.ExpenseReportDTO;
import SuperiorPro.SuperiorPOS.DTO.PageDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelExpenseReportExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelExpenseReportImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelExpenseReportImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.service.ExpenseReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/expense-reports")
@RequiredArgsConstructor
public class ExpenseReportController {

    private final ExpenseReportService expenseReportService;
    private final ExcelExpenseReportExportService excelExpenseReportExportService;
    private final ExcelExpenseReportImportService excelExpenseReportImportService;

    /** Create a new expense */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @PostMapping
    public ResponseEntity<ApiResponse> createExpense(@RequestBody ExpenseReportDTO expenseReportDTO) {
        ExpenseReportDTO response = expenseReportService.createExpense(expenseReportDTO);

        try {
            excelExpenseReportExportService.exportExpenseReportsToExcel();
        } catch (IOException e) {
            log.error("❌ Failed to export Excel after expense creation", e);
        }

        return ResponseEntity.ok(new ApiResponse("✅ Expense created successfully", response.getExpenseId()));
    }

    /** Get all unique expenseIds (summary list) */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/expenseIds")
    public ResponseEntity<List<ExpenseIdSummaryDTO>> getExpenseIds() {
        return ResponseEntity.ok(expenseReportService.getExpenseIdSummaries());
    }

    /** ExpenseId-level search with pagination */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/search")
    public ResponseEntity<PageDTO<ExpenseIdSummaryDTO>> searchExpenseIds(@RequestParam Map<String, String> params) {
        Page<ExpenseIdSummaryDTO> expenseIds = expenseReportService.getExpenseIds(params);
        return ResponseEntity.ok(new PageDTO<>(expenseIds));
    }

    /** Simple keyword search */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/search-by-keyword")
    public ResponseEntity<List<ExpenseReportDTO>> searchExpenseReportsByKeyword(@RequestParam String keyword) {
        return ResponseEntity.ok(expenseReportService.searchExpenseReportsByExpenseIdKeyword(keyword));
    }

    /** Cancel an entire expense (restore stock) */
    @PostMapping("/cancel/{expenseId}")
    public ResponseEntity<ApiResponse> cancelExpenseReport(@PathVariable String expenseId) {
        expenseReportService.cancelExpenseReportByExpenseId(expenseId);
        return ResponseEntity.ok(new ApiResponse("🗑️ Expense cancelled and stock restored", expenseId));
    }

    /** Update an expense (replace items) */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateExpenseReport(@RequestBody ExpenseReportDTO expenseReportDTO) {
        if (expenseReportDTO.getExpenseId() == null || expenseReportDTO.getExpenseId().isBlank()) {
            log.warn("❌ Update failed: missing expenseId");
            return ResponseEntity.badRequest().body(new ApiResponse("❌ ExpenseId is required for update", null));
        }
        if (expenseReportDTO.getItems() == null || expenseReportDTO.getItems().isEmpty()) {
            log.warn("❌ Update failed: no items provided");
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("❌ At least one item is required for update", expenseReportDTO.getExpenseId()));
        }
        if (expenseReportDTO.getSupplierName() == null || expenseReportDTO.getSupplierName().isBlank()) {
            expenseReportDTO.setSupplierName("General");
        }

        expenseReportService.updateExpenseReport(expenseReportDTO);
        return ResponseEntity.ok(new ApiResponse("✏️ ExpenseReport updated and stock adjusted", expenseReportDTO.getExpenseId()));
    }

    /** Paginated expenses list */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getExpenseReportsList(
            @RequestParam(defaultValue = "1") int _page,
            @RequestParam(defaultValue = "5") int _limit,
            @RequestParam(defaultValue = "expenseId") String _sortBy,
            @RequestParam(defaultValue = "desc") String _sortDir,
            @RequestParam(required = false) String expenseId) {
        return ResponseEntity.ok(expenseReportService.getPaginatedExpenseReports(_page, _limit, _sortBy, _sortDir, expenseId));
    }

    /** Get all expenses for a specific date */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/date/{date}")
    public ResponseEntity<List<ExpenseReportDTO>> getExpenseReportsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(expenseReportService.getExpenseReportsByDate(date));
    }

    /** Get a single expenseId (grouped DTO with items, totals) */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/expenseId/{expenseId}")
    public ResponseEntity<ExpenseReportDTO> getExpenseReportsByExpenseId(@PathVariable String expenseId) {
        return ResponseEntity.ok(expenseReportService.getExpenseReportsByExpenseId(expenseId));
    }

    /** Get all expenses within a date range */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/range")
    public ResponseEntity<List<ExpenseReportDTO>> getExpenseReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(expenseReportService.getExpenseReportsByDateRange(start, end));
    }

    /** Import expenses from Excel */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @PostMapping("/import")
    public ResponseEntity<String> importExpenseReportsFromExcel() {
        String filePath = ExcelPathResolver.resolveFixedPath("ExpenseReports");
        try {
            ImportSummary summary = excelExpenseReportImportService.importExpenseReportsFromExcel(filePath);
            StringBuilder response = new StringBuilder()
                    .append("📥 Imported expense reports from: ").append(filePath).append("\n")
                    .append("🔄 Updated: ").append(summary.updated()).append("\n")
                    .append("✅ Created: ").append(summary.created()).append("\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            log.info("📊 Import summary: {} created, {} updated, {} errors",
                    summary.created(), summary.updated(), summary.errors().size());

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }

    /** Export all expense reports to Excel */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/export")
    public ResponseEntity<String> exportExpenseReportsToExcel() {
        try {
            int recordCount = excelExpenseReportExportService.exportExpenseReportsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("ExpenseReports");

            log.info("📤 Exported {} expense report records to {}", recordCount, filePath);
            return ResponseEntity.ok("✅ Exported " + recordCount + " expense report records to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }

    /** Simple response wrapper */
    private record ApiResponse(String message, String expenseId) {}
}

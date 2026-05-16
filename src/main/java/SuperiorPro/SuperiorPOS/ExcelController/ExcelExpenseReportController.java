package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelExpenseReportExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelExpenseReportImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelExpenseReportImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.entity.ExpenseReport;
import SuperiorPro.SuperiorPOS.repository.ExpenseReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/expense-reports")
@RequiredArgsConstructor
public class ExcelExpenseReportController {

    private final ExcelExpenseReportExportService exportService;
    private final ExcelExpenseReportImportService importService;
    private final ExpenseReportRepository expenseReportRepository;

    /** Export all Expense Reports to Excel */
    @PostMapping("/export")
    public ResponseEntity<Map<String, Object>> exportExpenseReports() {
        try {
            int count = exportService.exportExpenseReportsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("ExpenseReports");
            log.info("📤 Successfully exported {} expense reports to {}", count, filePath);

            Map<String, Object> response = Map.of(
                "status", "success",
                "path", filePath,
                "count", count,
                "message", "Expense reports exported successfully"
            );
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("❌ Expense report export failed", e);
            Map<String, Object> error = Map.of(
                "status", "error",
                "message", "Export failed: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /** Import Expense Reports from Excel */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importExpenseReports() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("ExpenseReports");
            ImportSummary summary = importService.importExpenseReportsFromExcel(filePath);

            log.info("📊 Import summary — created: {}, updated: {}, errors: {}",
                summary.created(), summary.updated(), summary.errors().size());

            Map<String, Object> response = Map.of(
                "status", "success",
                "path", filePath,
                "created", summary.created(),
                "updated", summary.updated(),
                "errors", summary.errors(),
                "message", "Expense reports imported successfully"
            );
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("❌ Expense report import failed", e);
            Map<String, Object> error = Map.of(
                "status", "error",
                "message", "Import failed: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /** Preview Expense Reports (expanded fields) */
    @GetMapping("/preview")
    public ResponseEntity<List<Map<String, Object>>> previewReports() {
        List<ExpenseReport> reports = expenseReportRepository.findAll();

        List<Map<String, Object>> preview = reports.stream()
            .map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("expenseId", r.getExpenseId());
                m.put("supplierName", r.getSupplierName());
                m.put("payerName", r.getPayerName());
                m.put("expenseDate", r.getExpenseDate());
                m.put("expenseTime", r.getExpenseTime());
                m.put("productName", r.getProductName());
                m.put("expenseUnit", r.getExpenseUnit());
                m.put("expensePrice", r.getExpensePrice());
                m.put("expenseAmount", r.getExpenseAmount());
                m.put("source", r.getSource());
                return m;
            })
            .toList();

        log.info("🔍 Previewing {} expense report entries", preview.size());
        return ResponseEntity.ok(preview);
    }
}

package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCustomerExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCustomerImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCustomerImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/customers")
@RequiredArgsConstructor
@Validated
public class ExcelCustomerController {

    private final ExcelCustomerExportService excelCustomerExportService;
    private final ExcelCustomerImportService excelCustomerImportService;

    // 📤 Export customers to Excel
    @PostMapping("/export")
    public ResponseEntity<Map<String, Object>> exportCustomersToExcel() {
        try {
            int count = excelCustomerExportService.exportCustomersToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Customers");

            log.info("✅ Exported {} customers to {}", count, filePath);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "path", filePath,
                "count", count,
                "message", "Customers exported successfully"
            ));
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Export failed: " + e.getMessage()
            ));
        }
    }

    // 📥 Import customers from Excel
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCustomersFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Customers");
            ImportSummary summary = excelCustomerImportService.importCustomersFromExcel(filePath);

            log.info("✅ Imported customers: {} updated, {} created, {} errors",
                     summary.updated(), summary.created(), summary.errors().size());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "path", filePath,
                "created", summary.created(),
                "updated", summary.updated(),
                "errors", summary.errors(),
                "message", "Customers imported successfully"
            ));
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Import failed: " + e.getMessage()
            ));
        }
    }

    // 🔍 Preview customer names
    @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewCustomers() {
        List<String> names = excelCustomerImportService.previewCustomerNames();

        log.info("🔍 Previewed {} customers", names.size());

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "count", names.size(),
            "customers", names
        ));
    }
}

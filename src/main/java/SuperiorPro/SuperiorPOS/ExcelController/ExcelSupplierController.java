package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSupplierExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSupplierImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSupplierImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/suppliers")
@RequiredArgsConstructor
@Validated
public class ExcelSupplierController {

    private final ExcelSupplierExportService excelSupplierExportService;
    private final ExcelSupplierImportService excelSupplierImportService;

    // 📤 Export suppliers to Excel
    @PostMapping("/export")
    public ResponseEntity<Map<String, Object>> exportSuppliersToExcel() {
        try {
            int count = excelSupplierExportService.exportSuppliersToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Suppliers");

            log.info("📤 Exported {} suppliers to {}", count, filePath);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "path", filePath,
                "count", count,
                "message", "Suppliers exported successfully"
            ));
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Export failed: " + e.getMessage()
            ));
        }
    }

    // 📥 Import suppliers from Excel
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importSuppliersFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Suppliers");
            ImportSummary summary = excelSupplierImportService.importSuppliersFromExcel(filePath);

            log.info("📥 Import summary: {} created, {} updated, {} errors",
                     summary.created(), summary.updated(), summary.errors().size());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "path", filePath,
                "created", summary.created(),
                "updated", summary.updated(),
                "errors", summary.errors(),
                "message", "Suppliers imported successfully"
            ));
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Import failed: " + e.getMessage()
            ));
        }
    }

    // 🔍 Preview supplier names
    @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewSuppliers() {
        List<String> names = excelSupplierImportService.previewSupplierNames();

        log.info("🔍 Previewed {} suppliers", names.size());

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "count", names.size(),
            "suppliers", names
        ));
    }
}

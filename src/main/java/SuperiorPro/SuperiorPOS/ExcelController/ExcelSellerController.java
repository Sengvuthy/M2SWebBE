package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSellerExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSellerImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSellerImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/sellers")
@RequiredArgsConstructor
@Validated
public class ExcelSellerController {

    private final ExcelSellerExportService excelSellerExportService;
    private final ExcelSellerImportService excelSellerImportService;

    // 📤 Export Sellers
    @PostMapping("/export")
    public ResponseEntity<?> exportSellersToExcel() {
        try {
            int count = excelSellerExportService.exportSellersToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Sellers");

            log.info("✅ Exported {} sellers to {}", count, filePath);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Sellers exported successfully",
                "count", count,
                "path", filePath
            ));
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Export failed: " + e.getMessage()
            ));
        }
    }

    // 📥 Import Sellers
    @PostMapping("/import")
    public ResponseEntity<?> importSellersFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Sellers");
            ImportSummary summary = excelSellerImportService.importSellersFromExcel(filePath);

            log.info("📊 Import summary: {} updated, {} created, {} errors",
                     summary.updated(), summary.created(), summary.errors().size());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Sellers imported successfully",
                "path", filePath,
                "created", summary.created(),
                "updated", summary.updated(),
                "errors", summary.errors()
            ));
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Import failed: " + e.getMessage()
            ));
        }
    }

    // 👀 Preview Seller Names
    @GetMapping("/preview")
    public ResponseEntity<?> previewSellerNames() {
        List<String> names = excelSellerImportService.previewSellerNames();
        log.info("👀 Previewed {} seller names", names.size());

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "count", names.size(),
            "names", names
        ));
    }
}

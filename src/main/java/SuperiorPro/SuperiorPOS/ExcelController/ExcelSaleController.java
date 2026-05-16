package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/sales")
@RequiredArgsConstructor
public class ExcelSaleController {

    private final ExcelSaleExportService exportService;
    private final ExcelSaleImportService importService;

    // 🔹 Export Sales to Excel
    @PostMapping("/export")
    public ResponseEntity<String> exportToExcel() {
        try {
            int count = exportService.exportSalesToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Sales");
            log.info("📤 Exported {} sales to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " sales to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }

    // 🔹 Import Sales from Excel
    @PostMapping("/import")
    public ResponseEntity<String> importFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Sales");
            ImportSummary summary = importService.importSalesFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("📥 Imported sales from: ").append(filePath).append("\n")
                .append("⏭️ Duplicates skipped: ").append(summary.duplicates()).append("\n")
                .append("✅ Created: ").append(summary.created()).append("\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            log.info("📊 Import summary: {} created, {} duplicates, {} errors",
                summary.created(), summary.duplicates(), summary.errors().size());

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }
}

package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelPermissionExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelPermissionImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelPermissionImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/permissions")
@RequiredArgsConstructor
@Validated
public class ExcelPermissionController {

    private final ExcelPermissionExportService excelPermissionExportService;
    private final ExcelPermissionImportService excelPermissionImportService;

    // 📤 Export Permissions to Excel
    @PostMapping("/export")
    public ResponseEntity<String> exportPermissionsToExcel() {
        try {
            int count = excelPermissionExportService.exportPermissionsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Permissions");
            log.info("✅ Exported {} permissions to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " permissions to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }

    // 📥 Import Permissions from Excel (ID-based)
    @PostMapping("/import")
    public ResponseEntity<String> importPermissionsFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Permissions");
            ImportSummary summary = excelPermissionImportService.importPermissionsFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("📊 Import summary (ID-based):\n")
                .append("🔄 Updated: ").append(summary.updated()).append("\n")
                .append("✅ Created: ").append(summary.created()).append("\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            log.info("📊 Import completed: {} updated, {} created, {} errors",
                summary.updated(), summary.created(), summary.errors().size());

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }

    // 👀 Preview Permission Names
    @GetMapping("/preview")
    public ResponseEntity<List<String>> previewPermissions() {
        List<String> names = excelPermissionImportService.previewPermissionNames();
        log.info("👀 Previewed {} existing permissions", names.size());
        return ResponseEntity.ok(names);
    }
}

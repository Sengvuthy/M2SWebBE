package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserRoleExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserRoleImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserRoleImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/user-roles")
@RequiredArgsConstructor
public class ExcelUserRoleController {

    private final ExcelUserRoleExportService exportService;
    private final ExcelUserRoleImportService importService;

    @PostMapping("/export")
    public ResponseEntity<String> exportToExcel() {
        try {
            int count = exportService.exportUserRolesToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("UsersRole");
            log.info("Exported {} user-role mappings to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " user-role mappings to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("UsersRole");
            ImportSummary summary = importService.importUserRolesFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("✅ Imported user-role mappings:\n")
                .append("🔄 ").append(summary.updated()).append(" updated\n")
                .append("✅ ").append(summary.created()).append(" created\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ ").append(summary.errors().size()).append(" errors:\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            log.info("Import summary: {} updated, {} created, {} errors",
                summary.updated(), summary.created(), summary.errors().size());

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }
}

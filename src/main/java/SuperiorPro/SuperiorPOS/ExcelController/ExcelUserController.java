package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/users")
@RequiredArgsConstructor
public class ExcelUserController {

    private final ExcelUserExportService excelUserExportService;
    private final ExcelUserImportService excelUserImportService;

    @PostMapping("/export")
    public ResponseEntity<String> exportUsers() {
        try {
            int count = excelUserExportService.exportUsersToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Users");
            log.info("Exported {} users to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " users to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importUsers() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Users");
            ImportSummary summary = excelUserImportService.importUsersFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("✅ Imported users:\n")
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

    @GetMapping("/preview")
    public ResponseEntity<List<String>> previewUsernames() {
        List<String> usernames = excelUserImportService.previewUsernames();
        return ResponseEntity.ok(usernames);
    }
}

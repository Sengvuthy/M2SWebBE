package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRolePermissionExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRolePermissionImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRolePermissionImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/role-permissions")
@RequiredArgsConstructor
public class ExcelRolePermissionController {

    private final ExcelRolePermissionExportService exportService;
    private final ExcelRolePermissionImportService importService;

    @PostMapping("/export")
    public ResponseEntity<String> exportToExcel() {
        try {
            int count = exportService.exportRolePermissionsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("RolePermissions");
            log.info("Exported {} role-permission mappings to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " role-permission mappings to: " + filePath);
        } catch (IOException e) {
            log.error("Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("RolePermissions");
            ImportSummary summary = importService.importRolesPermissionFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("✅ Imported role-permission mappings:\n")
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
            log.error("Import failed", e);
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }
}

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
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRoleExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRoleImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRoleImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/roles")
@RequiredArgsConstructor
@Validated
public class ExcelRoleController {

    private final ExcelRoleExportService excelRoleExportService;
    private final ExcelRoleImportService excelRoleImportService;
    private final RoleRepository roleRepository;

    // 📤 Export Roles to Excel
    @PostMapping("/export")
    public ResponseEntity<String> exportRolesToExcel() {
        try {
            int count = excelRoleExportService.exportRolesToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Roles");
            log.info("✅ Exported {} roles to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " roles to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }

    // 📥 Import Roles from Excel (ID-based)
    @PostMapping("/import")
    public ResponseEntity<String> importRolesFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Roles");
            ImportSummary summary = excelRoleImportService.importRolesFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("📊 Import summary (ID-based):\n")
                .append("🔄 Updated: ").append(summary.updated()).append("\n")
                .append("✅ Created: ").append(summary.created()).append("\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            log.info("📊 Import summary: {} updated, {} created, {} errors",
                summary.updated(), summary.created(), summary.errors().size());

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }

    // 👀 Preview Roles
    @GetMapping("/preview")
    public ResponseEntity<List<String>> previewRoles() {
        List<String> preview = roleRepository.findAll().stream()
            .map(role -> "ID: " + role.getId() + " — " + role.getRoleName() +
                         (role.getDescription() != null ? " — " + role.getDescription() : ""))
            .toList();

        log.info("👀 Previewed {} roles", preview.size());
        return ResponseEntity.ok(preview);
    }
}

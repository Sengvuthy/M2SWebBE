package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCategoryExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCategoryImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCategoryImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/categories")
@RequiredArgsConstructor
@Validated
public class ExcelCategoryController {

    private final ExcelCategoryExportService excelCategoryExportService;
    private final ExcelCategoryImportService excelCategoryImportService;

    // 📤 Export Categories to Excel
    @PostMapping("/export")
    public ResponseEntity<String> exportCategoriesToExcel() {
        try {
            int count = excelCategoryExportService.exportCategoriesToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Categories");

            log.info("📤 Exported {} categories to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " categories to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("❌ Export failed: " + e.getMessage());
        }
    }

    // 📥 Import Categories from Excel
    @PostMapping("/import")
    public ResponseEntity<String> importCategoriesFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Categories");
            ImportSummary summary = excelCategoryImportService.importCategoriesFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("📥 Imported categories from: ").append(filePath).append("\n")
                .append("✅ Created: ").append(summary.created()).append("\n")
                .append("🔄 Updated: ").append(summary.updated()).append("\n");

            log.info("📥 Import summary: {} created, {} updated", summary.created(), summary.updated());
            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("❌ Import failed: " + e.getMessage());
        }
    }

    // 🔍 Preview Categories (names only)
    @GetMapping("/preview")
    public ResponseEntity<List<String>> previewCategories() {
        List<String> names = excelCategoryImportService.previewCategoryNames();
        log.info("🔍 Previewed {} categories", names.size());
        return ResponseEntity.ok(names);
    }
}

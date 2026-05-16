package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelProductExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelProductImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelProductImportService.ImportSummary;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/excel/products")
@RequiredArgsConstructor
public class ExcelProductController {

    private final ExcelProductExportService excelProductExportService;
    private final ExcelProductImportService excelProductImportService;

    @PostMapping("/export")
    public ResponseEntity<String> exportProductsToExcel() {
        try {
            int count = excelProductExportService.exportProductsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Products");
            return ResponseEntity.ok("✅ Exported " + count + " products to: " + filePath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Export failed: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importProductsFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Products");
            ImportSummary summary = excelProductImportService.importProductsFromExcel(filePath);
            return ResponseEntity.ok("✅ Imported products: " +
                summary.updated() + " updated, " + summary.created() + " created.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Import failed: " + e.getMessage());
        }
    }
}

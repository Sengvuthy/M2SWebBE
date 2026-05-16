package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelImportProductExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelImportProductImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelImportProductImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/product-imports")
@RequiredArgsConstructor
@Validated
public class ExcelImportProductController {

    private final ExcelImportProductExportService exportService;
    private final ExcelImportProductImportService importService;

    /**  Export product imports to Excel */
    @PostMapping("/export")
    public ResponseEntity<ExportResponse> exportProductImportsToExcel(
            @RequestParam(defaultValue = "ProductImports") String fileName) {
        try {
            int count = exportService.exportProductImportsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath(fileName);

            ExportResponse response = new ExportResponse(filePath, count, "Export successful");
            log.info("✅ Exported {} product import records to: {}", count, filePath);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ExportResponse(null, 0, "Export failed: " + e.getMessage()));
        }
    }

    /**  Import product imports from Excel */
    @PostMapping("/import")
    public ResponseEntity<ImportSummary> importProductImportsFromExcel(
            @RequestParam(defaultValue = "ProductImports") String fileName) {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath(fileName);
            ImportSummary summary = importService.importProductImportsFromExcel(filePath);

            log.info(" Import summary: {} created, {} updated, {} errors",
                    summary.created(), summary.updated(), summary.errors().size());

            summary.errors().forEach(error -> log.warn("❌ Import error: {}", error));

            return ResponseEntity.ok(summary);
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImportSummary(0, 0, List.of("Import failed: " + e.getMessage())));
        }
    }

    /**  Preview distinct imported barcodes */
    @GetMapping("/preview")
    public ResponseEntity<List<String>> previewImportedBarcodes() {
        List<String> barcodes = importService.previewImportedBarcodes();
        log.info(" Previewed {} imported barcodes", barcodes.size());
        return ResponseEntity.ok(barcodes);
    }

    /** Simple DTO for export responses */
    public record ExportResponse(String filePath, int recordCount, String message) {}
}

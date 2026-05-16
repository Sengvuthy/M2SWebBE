package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.DTO.CancelProductImportRequest;
import SuperiorPro.SuperiorPOS.DTO.ImportIdSummaryDTO;
import SuperiorPro.SuperiorPOS.DTO.PageDTO;
import SuperiorPro.SuperiorPOS.DTO.ProductImportDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelImportProductExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelImportProductImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelImportProductImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.service.ProductImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/product-imports")
@RequiredArgsConstructor
public class ProductImportController {

    private final ProductImportService productImportService;
    private final ExcelImportProductExportService excelImportProductExportService;
    private final ExcelImportProductImportService excelImportProductImportService;

    /** Create a new product import */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @PostMapping
    public ResponseEntity<ApiResponse> createImportProduct(@RequestBody ProductImportDTO productImportDTO) {
        ProductImportDTO response = productImportService.importProduct(productImportDTO);

        try {
            excelImportProductExportService.exportProductImportsToExcel();
        } catch (IOException e) {
            log.error("❌ Failed to export Excel after importing product", e);
        }

        return ResponseEntity.ok(new ApiResponse("✅ Product import created successfully", response.getImportId()));
    }

    /** Get all unique importIds (summary list) */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/importIds")
    public ResponseEntity<List<ImportIdSummaryDTO>> getImportIds() {
        return ResponseEntity.ok(productImportService.getImportIdSummaries());
    }

    /** ImportId-level search with pagination */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/search")
    public ResponseEntity<?> searchImportIds(@RequestParam Map<String, String> params) {
        Page<ImportIdSummaryDTO> importIds = productImportService.getImportIds(params);
        PageDTO<ImportIdSummaryDTO> pageDTO = new PageDTO<>(importIds);
        return ResponseEntity.ok(pageDTO);
    }

    /** Simple keyword search */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/search-by-keyword")
    public ResponseEntity<List<ProductImportDTO>> searchProductImportsByKeyword(@RequestParam String keyword) {
        return ResponseEntity.ok(productImportService.searchProductImportsByImportIdKeyword(keyword));
    }

    /** Cancel an entire import id (restore stock) */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse> cancelProductImport(@RequestBody CancelProductImportRequest request) {
        productImportService.cancelProductImportByImportId(request.getImportId());
        return ResponseEntity.ok(new ApiResponse("🗑️ Product import cancelled and stock restored", request.getImportId()));
    }

    /** Update an import id (replace items) */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateProductImport(@RequestBody ProductImportDTO productImportDTO) {
        if (productImportDTO.getImportId() == null || productImportDTO.getImportId().isBlank()) {
            log.warn("❌ Update failed: missing import id");
            return ResponseEntity.badRequest().body(new ApiResponse("❌ Import id is required for update", null));
        }
        if (productImportDTO.getItems() == null || productImportDTO.getItems().isEmpty()) {
            log.warn("❌ Update failed: no items provided");
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("❌ At least one item is required for update", productImportDTO.getImportId()));
        }
        if (productImportDTO.getSupplierName() == null || productImportDTO.getSupplierName().isBlank()) {
            productImportDTO.setSupplierName("General");
        }

        productImportService.updateProductImport(productImportDTO);
        return ResponseEntity.ok(new ApiResponse("✏️ Product import updated and stock adjusted", productImportDTO.getImportId()));
    }

    /** Paginated product imports list */
    @GetMapping("/list")
    public ResponseEntity<?> getProductImportsList(
            @RequestParam(defaultValue = "1") int _page,
            @RequestParam(defaultValue = "5") int _limit,
            @RequestParam(defaultValue = "importId") String _sortBy,
            @RequestParam(defaultValue = "desc") String _sortDir,
            @RequestParam(required = false) String importId) {
        return ResponseEntity.ok(productImportService.getPaginatedProductImports(_page, _limit, _sortBy, _sortDir, importId));
    }

    /** Get all product imports for a specific date */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/date/{date}")
    public ResponseEntity<List<ProductImportDTO>> getProductImportsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(productImportService.getProductImportsByDate(date));
    }

    /** Get a single import id (grouped DTO with items, totals) */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/importId/{importId}")
    public ResponseEntity<ProductImportDTO> getProductImportsByImportId(@PathVariable String importId) {
        return ResponseEntity.ok(productImportService.getProductImportsByImportId(importId));
    }

    /** Get all product imports within a date range */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/range")
    public ResponseEntity<List<ProductImportDTO>> getProductImportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(productImportService.getProductImportsByDateRange(start, end));
    }

    /** Import product imports from Excel */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @PostMapping("/import")
    public ResponseEntity<String> importProductImportsFromExcel() {
        String filePath = ExcelPathResolver.resolveFixedPath("ProductImports");
        try {
            ImportSummary summary = excelImportProductImportService.importProductImportsFromExcel(filePath);
            StringBuilder response = new StringBuilder()
                    .append("📥 Imported product imports from: ").append(filePath).append("\n")
                    .append("🔄 Updated: ").append(summary.updated()).append("\n")
                    .append("✅ Created: ").append(summary.created()).append("\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            log.info("📊 Import summary: {} created, {} updated, {} errors",
                    summary.created(), summary.updated(), summary.errors().size());

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }

    /** Export all product import reports to Excel */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_SELLER')")
    @GetMapping("/export")
    public ResponseEntity<String> exportImportProductReportsToExcel() {
        try {
            int recordCount = excelImportProductExportService.exportProductImportsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("ProductImports");

            log.info("📤 Exported {} product import records to {}", recordCount, filePath);
            return ResponseEntity.ok("✅ Exported " + recordCount + " product import records to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }

    /** Simple response wrapper */
    private record ApiResponse(String message, String importId) {}
}

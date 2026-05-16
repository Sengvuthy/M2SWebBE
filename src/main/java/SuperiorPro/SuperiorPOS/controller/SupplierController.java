package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.DTO.PageDTO;
import SuperiorPro.SuperiorPOS.DTO.SupplierDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSupplierExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSupplierImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSupplierImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.controller.SellerController.ApiResponse;
import SuperiorPro.SuperiorPOS.service.SupplierService;
import SuperiorPro.SuperiorPOS.service.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final ExcelSupplierImportService excelSupplierImportService;
    private final ExcelSupplierExportService excelSupplierExportService;

    // ✅ Create Supplier
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<SupplierDTO> createSupplier(@RequestBody SupplierDTO dto) {
        SupplierDTO saved = supplierService.save(dto);
        return ResponseEntity.ok(saved);
    }

    // ✅ Get Supplier by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id) {
        SupplierDTO supplier = supplierService.getById(id);
        return ResponseEntity.ok(supplier);
    }

    // ✅ Update Supplier by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SupplierDTO> updateSupplierById(
            @PathVariable Long id,
            @RequestBody SupplierDTO supplierDTO) {

        SupplierDTO updated = supplierService.updateById(id, supplierDTO);
        return ResponseEntity.ok(updated);
    }

    // ✅ Delete Supplier by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSupplierById(@PathVariable Long id) {
        supplierService.deleteById(id);
        log.info("🗑️ Deleted supplier ID {}", id);
        return ResponseEntity.ok("✅ Supplier deleted successfully!");
    }

    // ✅ Search Suppliers with Pagination
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<PageDTO<SupplierDTO>> searchSuppliers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name,asc") String sort) {

        String[] sortParts = sort.split(",");
        String sortField = sortParts.length > 0 ? sortParts[0] : "name";
        Sort.Direction direction = sortParts.length > 1
                ? Sort.Direction.fromString(sortParts[1])
                : Sort.Direction.ASC;

        Pageable pageable = PageUtil.getPageable(page, size, Sort.by(Sort.Order.by(sortField).with(direction)));
        Page<SupplierDTO> suppliers = supplierService.getSuppliers(name, pageable);

        PageDTO<SupplierDTO> pageDTO = new PageDTO<>(suppliers);
        return ResponseEntity.ok(pageDTO);
    }

    // 📥 Import Suppliers from Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<String> importSuppliersFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Suppliers");
            ImportSummary summary = excelSupplierImportService.importSuppliersFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("📥 Imported suppliers from: ").append(filePath).append("\n")
                .append("✅ Created: ").append(summary.created()).append("\n")
                .append("🔄 Updated: ").append(summary.updated()).append("\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            log.info("📥 Import summary: {} created, {} updated, {} errors",
                    summary.created(), summary.updated(), summary.errors().size());

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("❌ Import failed: " + e.getMessage());
        }
    }

    // 📤 Export Suppliers to Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<String> exportSuppliersToExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Suppliers");
            int count = excelSupplierExportService.exportSuppliersToExcel();

            log.info("📤 Exported {} suppliers to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " suppliers to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("❌ Export failed: " + e.getMessage());
        }
    }
}

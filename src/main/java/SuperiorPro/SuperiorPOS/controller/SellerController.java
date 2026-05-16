package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import SuperiorPro.SuperiorPOS.DTO.SellerDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSellerExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSellerImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSellerImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.entity.Seller;
import SuperiorPro.SuperiorPOS.exception.ErrorDTO;
import SuperiorPro.SuperiorPOS.mapper.SellerMapper;
import SuperiorPro.SuperiorPOS.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final SellerMapper sellerMapper;
    private final ExcelSellerImportService excelSellerImportService;
    private final ExcelSellerExportService excelSellerExportService;

    // ✅ Create Seller
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createSeller(@Valid @RequestBody SellerDTO dto) {
        Seller seller = sellerMapper.toEntity(dto);
        Seller saved = sellerService.save(seller);

        log.info("✅ Created seller ID {} with name '{}' and employeeCode '{}'",
                 saved.getId(), saved.getName(), saved.getEmployeeCode());

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
            "success",
            "Seller created successfully",
            sellerMapper.toDTO(saved)
        ));
    }

    // ✅ Get Seller by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getSellerById(@PathVariable Long id) {
        Seller seller = sellerService.getById(id);
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Seller retrieved successfully",
            sellerMapper.toDTO(seller)
        ));
    }

    // 🔎 Search Sellers (Paginated)
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<?> searchSellers(@RequestParam(defaultValue = "1") int _page,
                                           @RequestParam(defaultValue = "10") int _limit,
                                           @RequestParam(defaultValue = "id") String _sortBy,
                                           @RequestParam(defaultValue = "asc") String _sortDir,
                                           @RequestParam(required = false) String name) {
        log.info("🔎 Searching sellers with params: page={}, limit={}, sortBy={}, sortDir={}, name={}",
                 _page, _limit, _sortBy, _sortDir, name);

        try {
            Pageable pageable = PageRequest.of(
                _page - 1,
                _limit,
                Sort.by(Sort.Direction.fromString(_sortDir), _sortBy)
            );

            Page<Seller> sellers = sellerService.getSellers(name, pageable);
            PageDTO<SellerDTO> pageDTO = new PageDTO<>(sellers.map(sellerMapper::toDTO));

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "list", pageDTO.getList(),
                "paginationDTO", pageDTO.getPaginationDTO()
            ));
        } catch (Exception e) {
            log.error("❌ Search failed", e);
            return ResponseEntity.badRequest().body(new ErrorDTO("Invalid search parameters"));
        }
    }

    // ✅ Update Seller by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSellerById(@PathVariable Long id,
                                              @Valid @RequestBody SellerDTO dto) {
        Seller updated = sellerService.updateById(id, dto);

        log.info("✏️ Updated seller ID {} with employeeCode '{}'", updated.getId(), updated.getEmployeeCode());

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Seller updated successfully",
            sellerMapper.toDTO(updated)
        ));
    }

    // ✅ Delete Seller by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSellerById(@PathVariable Long id) {
        sellerService.deleteById(id);
        log.info("🗑️ Deleted seller ID {}", id);

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Seller deleted successfully",
            null
        ));
    }

    // 📥 Import Sellers from Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<?> importSellersFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Sellers");
            ImportSummary summary = excelSellerImportService.importSellersFromExcel(filePath);

            log.info("📊 Import summary: {} updated, {} created, {} errors",
                     summary.updated(), summary.created(), summary.errors().size());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "path", filePath,
                "created", summary.created(),
                "updated", summary.updated(),
                "errors", summary.errors()
            ));
        } catch (IOException e) {
            log.error("❌ Seller import failed", e);
            return ResponseEntity.internalServerError().body(new ErrorDTO("Import failed: " + e.getMessage()));
        }
    }

    // 📤 Export Sellers to Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<?> exportSellersToExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Sellers");
            int count = excelSellerExportService.exportSellersToExcel();

            log.info("📤 Exported {} sellers to {}", count, filePath);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "path", filePath,
                "count", count,
                "message", "Sellers exported successfully"
            ));
        } catch (IOException e) {
            log.error("❌ Seller export failed", e);
            return ResponseEntity.internalServerError().body(new ErrorDTO("Export failed: " + e.getMessage()));
        }
    }

    // ✅ Unified API Response DTO
    public record ApiResponse<T>(String status, String message, T data) {}
}

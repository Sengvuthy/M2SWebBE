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

import SuperiorPro.SuperiorPOS.DTO.CategoryDTO;
import SuperiorPro.SuperiorPOS.DTO.PageDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCategoryExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCategoryImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCategoryImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.service.CategoryService;
import SuperiorPro.SuperiorPOS.service.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ExcelCategoryImportService excelCategoryImportService;
    private final ExcelCategoryExportService excelCategoryExportService;

    // ✅ Create Category
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO dto) {
        CategoryDTO saved = categoryService.save(dto);
        return ResponseEntity.ok(saved);
    }

    // ✅ Get Category by ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getById(id);
        return ResponseEntity.ok(category);
    }

    // ✅ Update Category by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryDTO dto) {

        CategoryDTO updated = categoryService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    // ✅ Search Categories with Pagination
    @GetMapping("/search")
    public ResponseEntity<PageDTO<CategoryDTO>> searchCategories(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name,asc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1
                ? Sort.Direction.fromString(sortParts[1])
                : Sort.Direction.ASC;

        Sort sortObj = Sort.by(Sort.Order.by(sortParts[0]).with(direction));
        Pageable pageable = PageUtil.getPageable(page, size, sortObj);

        Page<CategoryDTO> categories = categoryService.getCategories(name, pageable);
        PageDTO<CategoryDTO> pageDTO = new PageDTO<>(categories);

        return ResponseEntity.ok(pageDTO);
    }

    // ✅ Delete Category by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteById(id);
        return ResponseEntity.ok("✅ Category deleted successfully");
    }

    // ✅ Import Categories from Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<String> importCategoriesFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Categories");
            ImportSummary summary = excelCategoryImportService.importCategoriesFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("📥 Imported categories from: ").append(filePath).append("\n")
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

    // ✅ Export Categories to Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<String> exportCategoriesToExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Categories");
            int count = excelCategoryExportService.exportCategoriesToExcel();

            log.info("📤 Exported {} categories to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " categories to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("❌ Export failed: " + e.getMessage());
        }
    }
}

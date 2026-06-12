package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.multipart.MultipartFile;

import SuperiorPro.SuperiorPOS.DTO.PageDTO;
import SuperiorPro.SuperiorPOS.DTO.ProductDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelProductExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelProductImportService;
import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.entity.ProductImport;
import SuperiorPro.SuperiorPOS.mapper.ProductMapper;
import SuperiorPro.SuperiorPOS.service.ProductService;
import SuperiorPro.SuperiorPOS.service.util.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final ExcelProductExportService excelProductExportService;
    private final ExcelProductImportService excelProductImportService;
    
    // Browse/search products (customers)
    @GetMapping
    public ResponseEntity<PageDTO<ProductDTO>> getProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            @RequestParam(value = "sort", defaultValue = "name,asc") String sort) {

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 ? Sort.Direction.fromString(sortParts[1]) : Sort.Direction.ASC;
        Pageable pageable = PageUtil.getPageable(page, size, Sort.by(Sort.Order.by(sortField).with(direction)));

        Page<Product> products;

        if (categoryId != null) {
            // ✅ filter by category
            products = productService.getProductsByCategory(categoryId, pageable);
        } else {
            // ✅ fallback to keyword search
            products = productService.searchByNameOrBarcode(keyword, pageable);
        }

        PageDTO<ProductDTO> pageDTO = new PageDTO<>(products.map(productMapper::toProductDTO));
        return ResponseEntity.ok(pageDTO);
    }

    // Get product detail by barcode (customers)
    @GetMapping("/{barcode}")
    public ResponseEntity<ProductDTO> getProductByBarcode(@PathVariable String barcode) {
        Product product = productService.getByBarcode(barcode);
        return ResponseEntity.ok(productMapper.toProductDTO(product));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product savedProduct = productService.save(productDTO);
        return ResponseEntity.ok(productMapper.toProductDTO(savedProduct));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PutMapping("/{barcode}")
    public ResponseEntity<ProductDTO> updateProductByBarcode(
            @PathVariable String barcode,
            @Valid @RequestBody ProductDTO productDTO) {
        Product updatedProduct = productService.updateByBarcode(barcode, productDTO);
        return ResponseEntity.ok(productMapper.toProductDTO(updatedProduct));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @DeleteMapping("/{barcode}")
    public ResponseEntity<String> deleteProductByBarcode(@PathVariable String barcode) {
        productService.deleteByBarcode(barcode);
        return ResponseEntity.ok("✅ Product deleted successfully!");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @DeleteMapping("/name/{name}")
    public ResponseEntity<String> deleteProductByName(@PathVariable String name) {
        productService.deleteByName(name);
        return ResponseEntity.ok("✅ Product deleted successfully!");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<String> importProductsFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Products");
            ExcelProductImportService.ImportSummary summary = excelProductImportService.importProductsFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("📥 Imported products from: ").append(filePath).append("\n")
                .append("✅ Created: ").append(summary.created()).append("\n")
                .append("🔄 Updated: ").append(summary.updated()).append("\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/{barcode}/upload-image")
    public ResponseEntity<String> uploadImage(
            @PathVariable String barcode,
            @RequestParam("file") MultipartFile file) {

        Product updated = productService.updateProductImage(barcode, file);
        return ResponseEntity.ok(updated.getImagePath()); // return filename only
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<String> exportProductsToExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Products");
            int count = excelProductExportService.exportProductsToExcel();
            return ResponseEntity.ok("✅ Exported " + count + " products to: " + filePath);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/import-history/barcode/{barcode}")
    public ResponseEntity<List<ProductImport>> getImportHistoryByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(productService.getImportHistoryByBarcode(barcode));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/import-history/name/{name}")
    public ResponseEntity<List<ProductImport>> getImportHistoryByName(@PathVariable String name) {
        return ResponseEntity.ok(productService.getImportHistoryByName(name));
    }

 // ✅ Upload product image
    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "folder", required = false) String folder) {
        try {
            String baseDir = "/uploads/products/";
            if (folder != null && !folder.isBlank()) {
                baseDir += folder + "/";
            }
            Files.createDirectories(Paths.get(baseDir));

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(baseDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/uploads/products/" + (folder != null ? folder + "/" : "") + fileName;
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}

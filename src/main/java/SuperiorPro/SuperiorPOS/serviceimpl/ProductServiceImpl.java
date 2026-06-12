package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import SuperiorPro.SuperiorPOS.DTO.CategoryDTO;
import SuperiorPro.SuperiorPOS.DTO.ProductDTO;
import SuperiorPro.SuperiorPOS.DTO.SupplierDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelProductExportService;
import SuperiorPro.SuperiorPOS.entity.ExpenseReport;
import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.entity.ProductImport;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.exception.ResourceNotFoundException;
import SuperiorPro.SuperiorPOS.mapper.ProductMapper;
import SuperiorPro.SuperiorPOS.repository.ExpenseReportRepository;
import SuperiorPro.SuperiorPOS.repository.ProductImportRepository;
import SuperiorPro.SuperiorPOS.repository.ProductRepository;
import SuperiorPro.SuperiorPOS.service.CategoryService;
import SuperiorPro.SuperiorPOS.service.ExpenseReportService;
import SuperiorPro.SuperiorPOS.service.ProductService;
import SuperiorPro.SuperiorPOS.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImportRepository importHistoryRepository;
    private final ExpenseReportRepository expenseReportRepository;
    private final ExpenseReportService expenseReportService;
    private final ProductMapper productMapper;
    private final ExcelProductExportService excelProductExportService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final String uploadDir = "/uploads/products";
    
    @Override
    public Product save(ProductDTO dto) {
        String barcode = trim(dto.getBarcode());
        validateNewProduct(barcode, dto);

        CategoryDTO category = categoryService.getById(dto.getCategoryId());
        SupplierDTO supplier = supplierService.getById(dto.getSupplierId());

        Product product = productMapper.toProduct(dto);
        product.setBarcode(barcode);
        product.setCategoryId(category.getId());
        product.setCategoryName(category.getName());
        product.setSupplierId(supplier.getId());
        product.setSupplierName(supplier.getName());

        Product saved = productRepository.save(product);
        log.info("✅ Saved product '{}' with category '{}' and supplier '{}'",
                saved.getName(), saved.getCategoryName(), saved.getSupplierName());

        recordInitialExpense(saved, dto);
        exportToExcel();
        return saved;
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));
    }

    @Override
    public Product getByName(String name) {
        return productRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Product", name));
    }

    @Override
    public Product getByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", barcode));
    }
    
    @Override
    public Page<Product> searchByNameOrBarcode(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.searchByNameOrBarcode(keyword.trim(), pageable);
    }

    @Override
    public Page<Product> getProducts(String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        String keyword = name.trim();
        return productRepository.findByNameContainingIgnoreCaseOrKhmerNameContainingIgnoreCase(keyword, keyword, pageable);
    }

    @Override
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        if (categoryId == null) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Category ID is required");
        }
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> getProductsBySupplier(Long supplierId, Pageable pageable) {
        if (supplierId == null) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Supplier ID is required");
        }
        return productRepository.findBySupplierId(supplierId, pageable);
    }

    @Override
    @Transactional
    public Product updateByBarcode(String barcode, ProductDTO dto) {
        Product product = getByBarcode(barcode);
        validateUpdate(dto);

        boolean salePriceChanged = dto.getSalePrice() != null && product.getSalePrice() != null
                && dto.getSalePrice().compareTo(product.getSalePrice()) != 0;

        // ✅ Handle barcode update safely
        if (dto.getBarcode() != null && !dto.getBarcode().isBlank()) {
            String newBarcode = dto.getBarcode().trim();
            Product existing = productRepository.findByBarcode(newBarcode).orElse(null);
            if (existing != null && !existing.getId().equals(product.getId())) {
                throw new API_Exception(HttpStatus.CONFLICT, "Another product already uses this barcode");
            }
            product.setBarcode(newBarcode);
        }

        if (dto.getCategoryId() != null) {
            CategoryDTO category = categoryService.getById(dto.getCategoryId());
            product.setCategoryId(category.getId());
            product.setCategoryName(category.getName());
        }
        if (dto.getSupplierId() != null) {
            SupplierDTO supplier = supplierService.getById(dto.getSupplierId());
            product.setSupplierId(supplier.getId());
            product.setSupplierName(supplier.getName());
        }

        // ✅ Normalize imagePath to filename only
        if (dto.getImagePath() != null && !dto.getImagePath().isBlank()) {
            String filename = Paths.get(dto.getImagePath()).getFileName().toString();
            product.setImagePath(filename);
        }

        product.setName(dto.getName());
        product.setKhmerName(dto.getKhmerName());
        product.setBuyPrice(dto.getBuyPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setAvailableUnit(dto.getAvailableUnit());

        Product updated = productRepository.save(product);

        if (salePriceChanged) {
            List<ProductImport> histories = importHistoryRepository.findByBarcode(product.getBarcode());
            for (ProductImport history : histories) {
                history.setSalePrice(dto.getSalePrice());
            }
            importHistoryRepository.saveAll(histories);
            log.info(" Synced sale price to {} import history entries for '{}'",
                    histories.size(), product.getName());
        }

        if (dto.getAvailableUnit() != null && dto.getBuyPrice() != null) {
            recordInitialExpense(updated, dto);
        }

        exportToExcel();
        return updated;
    }

    @Override
    @Transactional
    public void deleteByBarcode(String barcode) {
        Product product = getByBarcode(barcode);
        productRepository.delete(product);
        log.info("️ Deleted product with barcode '{}'", barcode);
        exportToExcel();
    }

    @Override
    @Transactional
    public void deleteByName(String name) {
        Product product = getByName(name);
        productRepository.delete(product);
        log.info("️ Deleted product with name '{}'", name);
        exportToExcel();
    }

    @Override
    public void validateStock(Long productId, BigDecimal numberOfUnit) {
        Product product = getById(productId);
        if (product.getAvailableUnit() == null || product.getAvailableUnit().compareTo(numberOfUnit) < 0) {
            throw new API_Exception(HttpStatus.BAD_REQUEST,
                    "Insufficient stock for product: " + product.getName());
        }
    }

    @Override
    public List<ProductImport> getImportHistoryByBarcode(String barcode) {
        return importHistoryRepository.findByBarcode(barcode);
    }

    @Override
    public List<ProductImport> getImportHistoryByName(String name) {
        return importHistoryRepository.findByProductNameIgnoreCase(name);
    }
    
    @Override
    @Transactional
    public Product updateProductImage(String barcode, MultipartFile file) {
        Product product = getByBarcode(barcode);

        // ✅ Delete old image if exists
        if (product.getImagePath() != null && !product.getImagePath().isBlank()) {
            Path oldFile = Paths.get(uploadDir, product.getImagePath());
            try {
                Files.deleteIfExists(oldFile);
                log.info("Deleted old image: {}", oldFile);
            } catch (IOException e) {
                log.warn("Could not delete old image: {}", oldFile, e);
            }
        }

        // ✅ Save new image
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
        Path target = Paths.get(uploadDir, filename);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new API_Exception(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save new image");
        }

        // ✅ Update DB with new filename only
        product.setImagePath(filename);
        return productRepository.save(product);
    }

    private void recordInitialExpense(Product product, ProductDTO dto) {
        BigDecimal unit = dto.getAvailableUnit();
        BigDecimal price = dto.getBuyPrice();

        if (unit == null || unit.compareTo(BigDecimal.ZERO) <= 0 || price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            log.info("ℹ️ Skipping expense report for product '{}' due to missing or invalid unit/price", product.getName());
            return;
        }

        BigDecimal totalAmount = price.multiply(unit);

        List<ExpenseReport> existing = expenseReportRepository.findByProductNameAndSource(product.getName(), "Creation Expense");
        ExpenseReport report = existing.isEmpty() ? new ExpenseReport() : existing.get(0);

        report.setExpenseId(existing.isEmpty() ? expenseReportService.generateNextExpenseId() : report.getExpenseId());
        report.setSupplierName(product.getSupplierName());
        report.setPayerName("System");
        report.setExpenseDate(LocalDate.now());
        report.setExpenseTime(LocalTime.now());
        report.setProductName(product.getName());
        report.setExpenseUnit(unit);
        report.setExpensePrice(price);
        report.setExpenseAmount(totalAmount);
        report.setSource("Creation Expense");

        expenseReportRepository.save(report);

        String action = existing.isEmpty() ? "Created" : "Updated";
        log.info(" {} creation expense for product '{}': {} units × {} = {} (source={})",
                action, product.getName(), unit, price, totalAmount, report.getSource());
    }

    private void exportToExcel() {
        try {
            excelProductExportService.exportProductsToExcel();
        } catch (IOException e) {
            log.error("❌ Failed to export products to Excel", e);
        }
    }

    private void validateNewProduct(String barcode, ProductDTO dto) {
        if (barcode == null || barcode.isEmpty()) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Barcode is required");
        }
        if (dto.getCategoryId() == null) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Category ID is required");
        }
        if (dto.getSupplierId() == null) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Supplier ID is required");
        }
        if (productRepository.existsByBarcode(barcode)) {
            throw new API_Exception(HttpStatus.CONFLICT, "Product with barcode already exists");
        }
        categoryService.getById(dto.getCategoryId()); // throws if not found
        supplierService.getById(dto.getSupplierId()); // throws if not found
    }

    private void validateUpdate(ProductDTO dto) {
        if (dto.getBuyPrice() != null && dto.getBuyPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Buy price must be non-negative");
        }
        if (dto.getSalePrice() != null && dto.getSalePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Sale price must be non-negative");
        }
        if (dto.getCategoryId() == null) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Category ID is required");
        }
        categoryService.getById(dto.getCategoryId()); // throws if not found
        if (dto.getSupplierId() == null) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Supplier ID is required");
        }
        supplierService.getById(dto.getSupplierId()); // throws if not found
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}

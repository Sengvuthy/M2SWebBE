package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.ImportIdSummaryDTO;
import SuperiorPro.SuperiorPOS.DTO.ProductImportDTO;
import SuperiorPro.SuperiorPOS.DTO.ProductImportItem;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelImportProductExportService;
import SuperiorPro.SuperiorPOS.entity.ExpenseReport;
import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.entity.ProductImport;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.mapper.ProductImportMapper;
import SuperiorPro.SuperiorPOS.repository.ExpenseReportRepository;
import SuperiorPro.SuperiorPOS.repository.ProductImportRepository;
import SuperiorPro.SuperiorPOS.repository.ProductRepository;
import SuperiorPro.SuperiorPOS.service.ExpenseReportService;
import SuperiorPro.SuperiorPOS.service.ProductImportService;
import SuperiorPro.SuperiorPOS.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductImportServiceImpl implements ProductImportService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ProductImportRepository importRepository;
    private final ExpenseReportRepository expenseRepository;
    private final ExpenseReportService expenseReportService;
    private final ProductImportMapper importMapper;
    private final ExcelImportProductExportService importHistoryExportService;

    @Override
    public ProductImportDTO importProduct(ProductImportDTO productImportDTO) {
        String importId = generateNextImportId();
        String expenseId = expenseReportService.generateNextExpenseId();

        String supplierName = Optional.ofNullable(productImportDTO.getSupplierName()).filter(s -> !s.isBlank()).orElse("General");
        String importerName = Optional.ofNullable(productImportDTO.getImporterName()).filter(s -> !s.isBlank()).orElse("Unknown");

        List<ProductImportItem> savedItems = new ArrayList<>();

        if (productImportDTO.getItems() != null && !productImportDTO.getItems().isEmpty()) {
        	for (ProductImportItem item : productImportDTO.getItems()) {
        	    Product product = productService.getByBarcode(item.getBarcode());

        	    // ✅ Update product sale price if provided
        	    if (item.getSalePrice() != null) {
        	        product.setSalePrice(item.getSalePrice());
        	        productRepository.save(product);
        	    }

        	    ProductImport productImport = new ProductImport();
        	    productImport.setImportId(importId);
        	    productImport.setImportDate(LocalDate.now());
        	    productImport.setImportTime(LocalTime.now());
        	    productImport.setSupplierName(supplierName);
        	    productImport.setImporterName(importerName);
        	    productImport.setBarcode(product.getBarcode());
        	    productImport.setProductName(product.getName());
        	    productImport.setKhmerName(product.getKhmerName());
        	    productImport.setImportUnit(item.getImportUnit());
        	    productImport.setBuyPrice(item.getBuyPrice());
        	    productImport.setBuyAmount(item.getBuyPrice().multiply(item.getImportUnit()));
        	    productImport.setSalePrice(item.getSalePrice());

        	    importRepository.save(productImport);
        	    productRepository.incrementAvailableUnit(item.getBarcode(), item.getImportUnit());

        	    savedItems.add(item);

        	    recordImportExpense(expenseId, supplierName, importerName,
        	            item.getProductName(), item.getKhmerName(),
        	            item.getImportUnit(), item.getBuyPrice());
        	}
        }

        log.info("✅ Product import recorded: supplier={}, importer={}", supplierName, importerName);
        exportToExcel();

        ProductImportDTO response = new ProductImportDTO();
        response.setImportId(importId);
        response.setSupplierName(supplierName);
        response.setImporterName(importerName);
        response.setImportDate(LocalDate.now());
        response.setImportTime(LocalTime.now());
        response.setItems(savedItems);

        return response;
    }

    @Override
    public List<ProductImportDTO> searchProductImportsByImportIdKeyword(String keyword) {
        return importRepository.findByImportIdContainingIgnoreCase(keyword).stream().map(this::toDTO).toList();
    }

    @Override
    public List<ImportIdSummaryDTO> getImportIdSummaries() {
        List<ProductImport> productImports = importRepository.findAll();
        Map<String, List<ProductImport>> grouped = productImports.stream()
                .collect(Collectors.groupingBy(ProductImport::getImportId));

        return grouped.entrySet().stream().map(entry -> {
            List<ProductImport> items = entry.getValue();
            ProductImport first = items.get(0);
            BigDecimal total = items.stream()
                    .map(ProductImport::getBuyAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ImportIdSummaryDTO(entry.getKey(), first.getSupplierName(), first.getImporterName(),
                    first.getImportDate(), first.getImportTime(), items.size(), total.doubleValue());
        }).sorted((a, b) -> b.getImportId().compareTo(a.getImportId())).toList();
    }

    @Override
    @Transactional
    public void cancelProductImportByImportId(String importId) {
        List<ProductImport> productImports = importRepository.findByImportId(importId);
        if (productImports.isEmpty()) {
            throw new API_Exception(HttpStatus.NOT_FOUND, "No product imports found for import id: " + importId);
        }

        for (ProductImport productImport : productImports) {
            productRepository.incrementAvailableUnit(productImport.getBarcode(), productImport.getImportUnit().negate());
            log.info("📦 Reduced {} units from product (barcode={}) due to import cancellation",
                    productImport.getImportUnit(), productImport.getBarcode());
        }

        importRepository.deleteAll(productImports);
        log.info("🗑️ Product Import cancelled: importId={}, cancelledItems={}", importId, productImports.size());
        exportToExcel();
    }

    @Override
    @Transactional
    public void updateProductImport(ProductImportDTO productImportDTO) {
        if (productImportDTO.getImportId() == null || productImportDTO.getImportId().isBlank()) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "ImportId is required for update");
        }

        List<ProductImport> existingImports = importRepository.findByImportId(productImportDTO.getImportId());
        if (existingImports.isEmpty()) {
            throw new API_Exception(HttpStatus.NOT_FOUND,
                    "No product imports found for import id: " + productImportDTO.getImportId());
        }

        // Roll back stock from old imports
        for (ProductImport oldImport : existingImports) {
            productRepository.incrementAvailableUnit(oldImport.getBarcode(), oldImport.getImportUnit().negate());
        }

        importRepository.deleteAll(existingImports);

        // Save new imports
        for (ProductImportItem item : productImportDTO.getItems()) {
            Product product = productService.getByBarcode(item.getBarcode());

            // ✅ Update product sale price if provided
            if (item.getSalePrice() != null) {
                product.setSalePrice(item.getSalePrice());
                productRepository.save(product);
            }

            ProductImport newImport = new ProductImport();
            newImport.setImportId(productImportDTO.getImportId());
            newImport.setImportDate(LocalDate.now());
            newImport.setImportTime(LocalTime.now());
            newImport.setSupplierName(productImportDTO.getSupplierName());
            newImport.setImporterName(productImportDTO.getImporterName());
            newImport.setBarcode(product.getBarcode());
            newImport.setProductName(product.getName());
            newImport.setKhmerName(product.getKhmerName());
            newImport.setImportUnit(item.getImportUnit());
            newImport.setBuyPrice(item.getBuyPrice());
            newImport.setBuyAmount(item.getBuyPrice().multiply(item.getImportUnit()));
            newImport.setSalePrice(item.getSalePrice()); // ✅ use new sale price

            importRepository.save(newImport);
            productRepository.incrementAvailableUnit(product.getBarcode(), item.getImportUnit());

            recordImportExpense(productImportDTO.getImportId(),
                    productImportDTO.getSupplierName(),
                    productImportDTO.getImporterName(),
                    product.getName(),
                    product.getKhmerName(),
                    item.getImportUnit(),
                    item.getBuyPrice());
        }

        log.info("✏️ Import Id {} overwritten with {} items", productImportDTO.getImportId(),
                productImportDTO.getItems().size());
        exportToExcel();
    }

    @Override
    public Map<String, Object> getPaginatedProductImports(int page, int limit, String sortBy, String sortDir,
                                                          String importId) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<ProductImport> productImportPage = (importId != null && !importId.isBlank())
                ? importRepository.findDistinctByImportIdContainingIgnoreCase(importId, pageable)
                : importRepository.findAll(pageable);

        List<ProductImportDTO> list = productImportPage.getContent().stream().map(this::toDTO).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("list", list);
        response.put("paginationDTO", Map.of(
                "totalPages", productImportPage.getTotalPages(),
                "totalElements", productImportPage.getTotalElements(),
                "currentPage", page));

        return response;
    }

    @Override
    public Page<ImportIdSummaryDTO> getImportIds(Map<String, String> params) {
        int page = Integer.parseInt(params.getOrDefault("_page", "1"));
        int limit = Integer.parseInt(params.getOrDefault("_limit", "5"));
        String sortBy = params.getOrDefault("_sortBy", "importId");
        String sortDir = params.getOrDefault("_sortDir", "desc");

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        String importIdFilter = params.get("importId");

        Page<String> importIdPage = importRepository.findDistinctImportIds(importIdFilter, pageable);

        List<ImportIdSummaryDTO> summaries = importIdPage.getContent().stream().map(imp -> {
            List<ProductImport> items = importRepository.findByImportId(imp);
            ProductImport first = items.get(0);
            BigDecimal total = items.stream()
                    .map(ProductImport::getBuyAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ImportIdSummaryDTO(
                    imp,
                    first.getSupplierName(),
                    first.getImporterName(),
                    first.getImportDate(),
                    first.getImportTime(),
                    items.size(),
                    total.doubleValue()
            );
        }).toList();

        return new PageImpl<>(summaries, pageable, importIdPage.getTotalElements());
    }

    @Override
    public ProductImportDTO getProductImportsByImportId(String importId) {
        List<ProductImport> productImports = importRepository.findByImportId(importId);
        if (productImports.isEmpty()) return null;

        ProductImport first = productImports.get(0);
        ProductImportDTO dto = new ProductImportDTO();
        dto.setImportId(first.getImportId());
        dto.setSupplierName(first.getSupplierName());
        dto.setImporterName(first.getImporterName());
        dto.setImportDate(first.getImportDate());
        dto.setImportTime(first.getImportTime());

        List<ProductImportItem> items = productImports.stream().map(pi -> {
            ProductImportItem item = new ProductImportItem();
            item.setBarcode(pi.getBarcode());
            item.setProductName(pi.getProductName());
            item.setKhmerName(pi.getKhmerName());
            item.setImportUnit(pi.getImportUnit());
            item.setBuyPrice(pi.getBuyPrice());
            item.setSalePrice(pi.getSalePrice());
            return item;
        }).toList();

        dto.setItems(items);
        BigDecimal total = productImports.stream()
                .map(ProductImport::getBuyAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setBuyAmount(total);

        return dto;
    }

    @Override
    public List<ProductImportDTO> getProductImportsByDate(LocalDate date) {
        return importRepository.findByImportDate(date).stream().map(this::toDTO).toList();
    }

    @Override
    public List<ProductImportDTO> getProductImportsByDateRange(LocalDate start, LocalDate end) {
        return importRepository.findByImportDateBetween(start, end).stream().map(this::toDTO).toList();
    }

    private ProductImportDTO toDTO(ProductImport productImport) {
        ProductImportDTO dto = new ProductImportDTO();
        dto.setImportId(productImport.getImportId());
        dto.setBarcode(productImport.getBarcode());
        dto.setProductName(productImport.getProductName());
        dto.setKhmerName(productImport.getKhmerName());
        dto.setImportUnit(productImport.getImportUnit());
        dto.setBuyAmount(productImport.getBuyAmount());
        dto.setImportDate(productImport.getImportDate());
        dto.setImportTime(productImport.getImportTime());
        dto.setSupplierName(productImport.getSupplierName());
        dto.setImporterName(productImport.getImporterName());
        return dto;
    }

    private String generateNextImportId() {
        ProductImport lastProductImport = importRepository.findTopByOrderByIdDesc();
        String lastImportId = (lastProductImport != null) ? lastProductImport.getImportId() : null;
        int nextNumber = 1;

        if (lastImportId != null && lastImportId.startsWith("IMP-")) {
            try {
                nextNumber = Integer.parseInt(lastImportId.substring(4)) + 1;
            } catch (NumberFormatException e) {
                log.warn("⚠️ ImportId format corrupted: {}", lastImportId);
            }
        }

        return String.format("IMP-%04d", nextNumber);
    }

    public String generateNextExpenseId() {
        ExpenseReport lastExpenseReport = expenseRepository.findTopByOrderByIdDesc();
        String lastExpenseId = (lastExpenseReport != null) ? lastExpenseReport.getExpenseId() : null;
        int nextNumber = 1;

        if (lastExpenseId != null && lastExpenseId.startsWith("EXP-")) {
            try {
                nextNumber = Integer.parseInt(lastExpenseId.substring(4)) + 1;
            } catch (NumberFormatException e) {
                log.warn("⚠️ ExpenseId format corrupted: {}", lastExpenseId);
            }
        }

        return String.format("EXP-%04d", nextNumber);
    }

    private void recordImportExpense(String expenseId, String supplierName, String importerName,
                                     String productName, String khmerName,
                                     BigDecimal unit, BigDecimal price) {
        if (unit == null || unit.compareTo(BigDecimal.ZERO) <= 0
                || price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("ℹ️ Skipping expense report for product '{}' due to invalid unit/price", productName);
            return;
        }

        BigDecimal totalAmount = price.multiply(unit);

        ExpenseReport expense = new ExpenseReport();
        expense.setExpenseId(expenseId);
        expense.setExpenseDate(LocalDate.now());
        expense.setExpenseTime(LocalTime.now());
        expense.setSupplierName(supplierName);
        expense.setPayerName(importerName);
        expense.setProductName(productName);
        expense.setExpenseUnit(unit);
        expense.setExpensePrice(price);
        expense.setExpenseAmount(totalAmount);
        expense.setSource("Importation");

        expenseRepository.save(expense);
        log.info("🧾 Recorded import expense: {} units × {} = {} (source={}, expenseId={})",
                unit, price, totalAmount, expense.getSource(), expenseId);
    }

    private void exportToExcel() {
        try {
            int recordCount = importHistoryExportService.exportProductImportsToExcel();
            log.info("📤 Export completed successfully — {} product import records exported to Excel", recordCount);
        } catch (IOException e) {
            log.error("❌ Export to Excel failed after product import mutation. Reason: {}", e.getMessage(), e);
        }
    }
}

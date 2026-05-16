package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelProductImportService {

    private final ProductRepository productRepository;

    public ImportSummary importProductsFromExcel(String filePath) throws IOException {
        if (!filePath.endsWith(".xlsx")) {
            throw new IOException("Invalid file format. Expected .xlsx");
        }

        int created = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("📥 Starting product import from Excel: {}", filePath);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                int excelRowNum = row.getRowNum() + 1;

                String barcode = getCellValue(row, 0);
                String name = getCellValue(row, 1);
                String khmerName = getCellValue(row, 2);
                BigDecimal unit = parseDecimal(row.getCell(3));
                BigDecimal buyPrice = parseDecimal(row.getCell(4));
                BigDecimal salePrice = parseDecimal(row.getCell(5));
                String categoryName = getCellValue(row, 6);
                String imagePath = getCellValue(row, 7);

                // Validate required fields
                if (isMissing(barcode, name, categoryName)) {
                    log.warn("❌ Skipping row {} due to missing required fields", excelRowNum);
                    if (barcode.isEmpty()) errors.add("❌ Excel row " + excelRowNum + ": Missing barcode");
                    if (name.isEmpty()) errors.add("❌ Excel row " + excelRowNum + ": Missing product name");
                    if (categoryName.isEmpty()) errors.add("❌ Excel row " + excelRowNum + ": Missing category name");
                    continue;
                }

                try {
                    Optional<Product> existing = productRepository.findByBarcode(barcode);
                    Product product = existing.orElseGet(Product::new);

                    product.setBarcode(barcode);
                    product.setName(name);
                    product.setKhmerName(khmerName);
                    product.setAvailableUnit(unit);
                    product.setBuyPrice(buyPrice);
                    product.setSalePrice(salePrice);
                    product.setCategoryName(categoryName);
                    product.setImagePath(imagePath);

                    productRepository.save(product);

                    if (existing.isPresent()) {
                        updated++;
                        log.info("🔄 Updated product: {}", barcode);
                    } else {
                        created++;
                        log.info("✅ Created new product: {}", barcode);
                    }

                } catch (Exception e) {
                    errors.add("❌ Error at Excel row " + excelRowNum + ": " + e.getMessage());
                    log.error("❌ Failed to process row {}", excelRowNum, e);
                }
            }

            log.info("📊 Import completed: {} updated, {} created, {} errors", updated, created, errors.size());
            return new ImportSummary(created, updated, errors);
        }
    }

    private boolean isMissing(String barcode, String name, String categoryName) {
        return barcode.isEmpty() || name.isEmpty() || categoryName.isEmpty();
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    private Integer parseInteger(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.NUMERIC)
            ? (int) cell.getNumericCellValue()
            : null;
    }

    private BigDecimal parseDecimal(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.NUMERIC)
            ? BigDecimal.valueOf(cell.getNumericCellValue())
            : BigDecimal.ZERO;
    }

    public record ImportSummary(int created, int updated, List<String> errors) {}
}

package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.entity.ProductImport;
import SuperiorPro.SuperiorPOS.repository.ProductImportRepository;
import SuperiorPro.SuperiorPOS.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportProductImportService {

    private final ProductRepository productRepository;
    private final ProductImportRepository importRepository;

    /**
     * Import product imports from an Excel file (.xlsx).
     * @param filePath path to the Excel file
     * @return summary of created/updated records and errors
     */
    public ImportSummary importProductImportsFromExcel(String filePath) throws IOException {
        if (!filePath.endsWith(".xlsx")) {
            throw new IOException("Invalid file format. Please provide an .xlsx file.");
        }

        int createdCount = 0;
        int updatedCount = 0;
        List<String> errors = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("📥 Starting import from Excel file: {}", filePath);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header

                String importId     = getCellValue(row, 1);
                String barcode      = getCellValue(row, 2);
                String productName  = getCellValue(row, 3);
                String khmerName	= getCellValue(row, 4);
                String importUnitRaw= getCellValue(row, 5);
                String buyPriceRaw  = getCellValue(row, 6);
                String buyAmountRaw = getCellValue(row, 7);
                String salePriceRaw = getCellValue(row, 8);
                String importDateRaw= getCellValue(row, 9);
                String importTimeRaw= getCellValue(row, 10);
                String importerName = getCellValue(row, 11);

                if (barcode.isBlank()) {
                    errors.add("Row " + (row.getRowNum() + 1) + ": Missing barcode");
                    continue;
                }

                Optional<Product> productOpt = productRepository.findByBarcode(barcode);
                if (productOpt.isEmpty()) {
                    errors.add("Row " + (row.getRowNum() + 1) + ": Product not found for barcode " + barcode);
                    continue;
                }

                try {
                	BigDecimal importUnit = new BigDecimal(importUnitRaw);
                    BigDecimal buyPrice = new BigDecimal(buyPriceRaw);
                    BigDecimal salePrice = new BigDecimal(salePriceRaw);

                    LocalDate importDate;
                    try {
                        importDate = LocalDate.parse(importDateRaw);
                    } catch (Exception e) {
                        importDate = LocalDate.now();
                        log.warn("⚠️ Row {}: Invalid date '{}', defaulting to today", row.getRowNum() + 1, importDateRaw);
                    }

                    LocalTime importTime;
                    try {
                        importTime = LocalTime.parse(importTimeRaw);
                    } catch (Exception e) {
                        importTime = LocalTime.now();
                        log.warn("⚠️ Row {}: Invalid time '{}', defaulting to now", row.getRowNum() + 1, importTimeRaw);
                    }

                    // ✅ Calculate buyAmount
                    BigDecimal buyAmountCalculated = buyPrice.multiply(importUnit);

                    // ✅ Cross-check with Excel Buy Amount column
                    try {
                        BigDecimal buyAmountExcel = new BigDecimal(buyAmountRaw);
                        if (buyAmountCalculated.compareTo(buyAmountExcel) != 0) {
                            log.warn("⚠️ Row {}: Calculated buyAmount={} differs from Excel value={}",
                                     row.getRowNum() + 1, buyAmountCalculated, buyAmountExcel);
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Row {}: Invalid Buy Amount '{}', using calculated value instead",
                                 row.getRowNum() + 1, buyAmountRaw);
                    }

                    List<ProductImport> existingRecords = importRepository.findByBarcodeAndImportDate(barcode, importDate);

                    ProductImport record;
                    if (existingRecords.size() == 1) {
                        record = existingRecords.get(0);
                        updatedCount++;
                        log.info("🔄 Updated row {}: {} units of '{}'", row.getRowNum() + 1, importUnit, productName);
                    } else if (existingRecords.isEmpty()) {
                        record = new ProductImport();
                        createdCount++;
                        log.info("✅ Created row {}: {} units of '{}'", row.getRowNum() + 1, importUnit, productName);
                    } else {
                        errors.add("Row " + (row.getRowNum() + 1) + ": Multiple records found for barcode=" + barcode + " on " + importDate);
                        continue;
                    }

                    String supplierName = Optional.ofNullable(productOpt.get().getSupplierName()).orElse("General");

                    record.setImportId(importId);
                    record.setBarcode(barcode);
                    record.setProductName(productName);
                    record.setKhmerName(khmerName);
                    record.setImportUnit(importUnit);
                    record.setBuyPrice(buyPrice);
                    record.setBuyAmount(buyAmountCalculated); // always trust calculated
                    record.setSalePrice(salePrice);
                    record.setImportDate(importDate);
                    record.setImportTime(importTime);
                    record.setImporterName(importerName.isBlank() ? "ExcelImport" : importerName);
                    record.setSupplierName(supplierName);

                    importRepository.save(record);

                } catch (Exception e) {
                    errors.add("Row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    log.warn("⚠️ Failed to import row {}: {}", row.getRowNum() + 1, e.getMessage());
                }
            }

            log.info("📊 Import completed: {} created, {} updated, {} errors", createdCount, updatedCount, errors.size());
            return new ImportSummary(createdCount, updatedCount, errors);

        } catch (Exception e) {
            throw new IOException("❌ Import failed: " + e.getMessage(), e);
        }
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> evaluateFormula(cell);
            default -> "";
        };
    }

    private String evaluateFormula(Cell cell) {
        try {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            CellValue value = evaluator.evaluate(cell);
            return switch (value.getCellType()) {
                case STRING -> value.getStringValue().trim();
                case NUMERIC -> String.valueOf((long) value.getNumberValue());
                case BOOLEAN -> String.valueOf(value.getBooleanValue());
                default -> "";
            };
        } catch (Exception e) {
            log.warn("⚠️ Formula evaluation failed at row {} col {}: {}", cell.getRowIndex() + 1, cell.getColumnIndex(), e.getMessage());
            return "";
        }
    }

    /** Preview distinct barcodes already imported */
    public List<String> previewImportedBarcodes() {
        return importRepository.findAll().stream()
                .map(ProductImport::getBarcode)
                .distinct()
                .collect(Collectors.toList());
    }

    /** Summary record for import results */
    public record ImportSummary(int created, int updated, List<String> errors) {}
}

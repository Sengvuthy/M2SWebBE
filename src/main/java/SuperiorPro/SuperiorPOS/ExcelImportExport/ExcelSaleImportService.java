package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.Sale;
import SuperiorPro.SuperiorPOS.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelSaleImportService {

    private final SaleRepository saleRepository;

    public ImportSummary importSalesFromExcel(String filePath) throws IOException {
        if (!filePath.endsWith(".xlsx")) {
            throw new IOException("Invalid file format. Expected .xlsx");
        }

        int created = 0;
        int skippedDuplicates = 0;
        List<String> errors = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("📥 Starting sale import from Excel: {}", filePath);

            // 🔹 Validate header row
            validateHeaders(sheet.getRow(0));

            // 🔹 Parse rows
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header

                try {
                    String invoice = getCellValue(row, 0);
                    String barcode = getCellValue(row, 1);
                    String product = getCellValue(row, 2);
                    String khmerName = getCellValue(row, 3);
                    BigDecimal units = getDecimalCellValue(row, 4);
                    BigDecimal unitPrice = getDecimalCellValue(row, 5);
                    BigDecimal amount = getDecimalCellValue(row, 6);
                    String customer = getCellValue(row, 7);
                    LocalDate saleDate = LocalDate.parse(getCellValue(row, 8));
                    LocalTime saleTime = LocalTime.parse(getCellValue(row, 9));

                    // 🔄 Duplicate check
                    if (saleRepository.existsByInvoiceAndBarcode(invoice, barcode)) {
                        skippedDuplicates++;
                        skipped.add("Row " + (row.getRowNum() + 1) + ": Duplicate [Invoice=" + invoice + ", Barcode=" + barcode + "]");
                        continue;
                    }

                    Sale sale = new Sale();
                    sale.setInvoice(invoice);
                    sale.setBarcode(barcode);
                    sale.setProductName(product);
                    sale.setKhmerName(khmerName);
                    sale.setNumberOfUnit(units);
                    sale.setUnitPrice(unitPrice);
                    sale.setSoldAmount(amount);

                    // ✅ Ensure customerId is set
                    Long customerId = 1L; // fallback to General
                    sale.setCustomerId(customerId);
                    sale.setCustomerName(customer != null && !customer.isBlank() ? customer : "General");

                    sale.setSaleDate(saleDate);
                    sale.setSaleTime(saleTime);

                    saleRepository.save(sale);
                    created++;

                    log.info("✅ Imported sale: invoice={}, product={}, units={}", invoice, product, units);

                } catch (Exception e) {
                    String errorMsg = "Row " + (row.getRowNum() + 1) + ": " + e.getMessage();
                    errors.add(errorMsg);
                    log.warn("⚠️ Failed to import row {}: {}", row.getRowNum() + 1, e.getMessage());
                }
            }

            log.info("📊 Sale import completed: ✅ {} created, ⏭️ {} duplicates skipped, ❌ {} errors",
                     created, skippedDuplicates, errors.size());

            return new ImportSummary(created, skippedDuplicates, errors, skipped);
        }
    }

    // 🔹 Validate header row
    private void validateHeaders(Row header) throws IOException {
        String[] expectedHeaders = {
            "Invoice", "Barcode", "Product", "Khmer Name",
            "Units Sold", "Unit Price", "Sold Amount",
            "Customer", "Sale Date", "Sale Time"
        };

        for (int i = 0; i < expectedHeaders.length; i++) {
            String actual = getCellValue(header, i);
            if (!actual.equalsIgnoreCase(expectedHeaders[i])) {
                throw new IOException("Invalid header at column " + (i + 1) +
                        ". Expected: '" + expectedHeaders[i] + "', but found: '" + actual + "'");
            }
        }
    }

    // 🔹 Cell value readers
    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private BigDecimal getDecimalCellValue(Row row, int index) {
        String value = getCellValue(row, index);
        return value.isEmpty() ? BigDecimal.ZERO : new BigDecimal(value);
    }

    // 🔹 Import summary record
    public record ImportSummary(int created, int duplicates, List<String> errors, List<String> skipped) {}
}

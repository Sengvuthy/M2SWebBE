package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.ExpenseReport;
import SuperiorPro.SuperiorPOS.repository.ExpenseReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExpenseReportImportService {

    private final ExpenseReportRepository expenseReportRepository;

    public ImportSummary importExpenseReportsFromExcel(String filePath) throws IOException {
        if (!filePath.endsWith(".xlsx")) {
            throw new IOException("Invalid file format. Expected .xlsx");
        }

        int created = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("📥 Starting expense report import from Excel: {}", filePath);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header
                int rowNum = row.getRowNum() + 1;

                String expenseId     = getCellValue(row, 1);
                String supplierName  = getCellValue(row, 2);
                String payerName     = getCellValue(row, 3);
                LocalDate expenseDate= getDateValue(row, 4);
                String expenseTime   = getCellValue(row, 5);
                String productName   = getCellValue(row, 6);
                BigDecimal expenseUnit= getDecimalValue(row, 7);
                BigDecimal expensePrice = getDecimalValue(row, 8);
                BigDecimal expenseAmount= getDecimalValue(row, 9);
                String source        = getCellValue(row, 10);

                if (expenseId.isEmpty() || supplierName.isEmpty() || payerName.isEmpty()
                        || expenseDate == null || expenseUnit == null || expensePrice == null || expenseAmount == null) {
                    errors.add("❌ Row " + rowNum + ": Missing required fields");
                    log.warn("⚠️ Row {} missing fields", rowNum);
                    continue;
                }

                // 🔍 Check existing reports
                List<ExpenseReport> matches = expenseReportRepository.findByExpenseId(expenseId);

                if (matches.size() == 1) {
                    ExpenseReport report = matches.get(0);
                    report.setSupplierName(supplierName);
                    report.setPayerName(payerName);
                    report.setExpenseDate(expenseDate);
                    report.setExpenseTime(expenseTime.isEmpty() ? LocalTime.now() : LocalTime.parse(expenseTime));
                    report.setProductName(productName);
                    report.setExpenseUnit(expenseUnit);
                    report.setExpensePrice(expensePrice);
                    report.setExpenseAmount(expenseAmount);
                    report.setSource(source);
                    expenseReportRepository.save(report);
                    updated++;
                    log.info("🔄 Row {}: Updated expense report '{}'", rowNum, expenseId);
                } else if (matches.isEmpty()) {
                    ExpenseReport report = new ExpenseReport();
                    report.setExpenseId(expenseId);
                    report.setSupplierName(supplierName);
                    report.setPayerName(payerName);
                    report.setExpenseDate(expenseDate);
                    report.setExpenseTime(expenseTime.isEmpty() ? LocalTime.now() : LocalTime.parse(expenseTime));
                    report.setProductName(productName);
                    report.setExpenseUnit(expenseUnit);
                    report.setExpensePrice(expensePrice);
                    report.setExpenseAmount(expenseAmount);
                    report.setSource(source);
                    expenseReportRepository.save(report);
                    created++;
                    log.info("✅ Row {}: Created new expense report '{}'", rowNum, expenseId);
                } else {
                    errors.add("⚠️ Row " + rowNum + ": Multiple reports found for expenseId '" + expenseId + "'");
                    log.warn("⚠️ Row {}: Multiple expense reports found for expenseId '{}'", rowNum, expenseId);
                }
            }

            log.info("📊 Import summary: {} created, {} updated, {} errors", created, updated, errors.size());
            return new ImportSummary(created, updated, errors);
        }
    }

    // 🔧 Helpers
    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal getDecimalValue(Row row, int index) {
        Cell cell = row.getCell(index);
        return (cell != null && cell.getCellType() == CellType.NUMERIC)
            ? BigDecimal.valueOf(cell.getNumericCellValue())
            : null;
    }

    private LocalDate getDateValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        if (cell.getCellType() == CellType.STRING) {
            try {
                return LocalDate.parse(cell.getStringCellValue().trim());
            } catch (Exception e) {
                log.warn("⚠️ Row {}: Invalid date format '{}'", row.getRowNum() + 1, cell.getStringCellValue());
            }
        }

        return null;
    }

    /** Summary record returned after import */
    public record ImportSummary(int created, int updated, List<String> errors) {}
}

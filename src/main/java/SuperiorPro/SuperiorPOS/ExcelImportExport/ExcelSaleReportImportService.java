package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.MonthlySaleReport;
import SuperiorPro.SuperiorPOS.entity.SaleReport;
import SuperiorPro.SuperiorPOS.entity.YearlySaleReport;
import SuperiorPro.SuperiorPOS.repository.MonthlySaleReportRepository;
import SuperiorPro.SuperiorPOS.repository.SaleReportRepository;
import SuperiorPro.SuperiorPOS.repository.YearlySaleReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelSaleReportImportService {

    private final SaleReportRepository saleReportRepository;
    private final MonthlySaleReportRepository monthlyRepo;
    private final YearlySaleReportRepository yearlyRepo;

    public ImportSummary importSaleReportsFromExcel(String filePath) throws IOException {
        if (!filePath.endsWith(".xlsx")) {
            throw new IOException("Invalid file format. Expected .xlsx");
        }

        int created = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (Sheet sheet : workbook) {
                String sheetName = sheet.getSheetName().toLowerCase();

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // skip header

                    try {
                        if (sheetName.contains("daily")) {
                            Result result = importDaily(row);
                            created += result.created;
                            updated += result.updated;
                        } else if (sheetName.contains("monthly")) {
                            Result result = importMonthly(row);
                            created += result.created;
                            updated += result.updated;
                        } else if (sheetName.contains("yearly")) {
                            Result result = importYearly(row);
                            created += result.created;
                            updated += result.updated;
                        }
                    } catch (Exception e) {
                        errors.add("❌ Row " + (row.getRowNum() + 1) + " in '" + sheetName + "' failed: " + e.getMessage());
                    }
                }
            }

            log.info("📊 Sale report import completed: {} created, {} updated, {} errors", created, updated, errors.size());
            return new ImportSummary(created, updated, errors);
        }
    }

    private Result importDaily(Row row) {
        LocalDate date = getDateValue(row, 0);
        BigDecimal amount = getDecimalValue(row, 1);
        BigDecimal units = getDecimalValue(row, 2);
        int transactions = getIntValue(row, 3);

        if (date == null) throw new IllegalArgumentException("Missing report date");

        Optional<SaleReport> existing = saleReportRepository.findByReportDate(date);
        SaleReport report = existing.orElse(new SaleReport());
        report.setReportDate(date);
        report.setTotalSalesAmount(amount);
        report.setTotalUnitsSold(units);
        report.setTotalTransactions(transactions);
        saleReportRepository.save(report);

        return existing.isPresent() ? new Result(0, 1) : new Result(1, 0);
    }

    private Result importMonthly(Row row) {
        int year = getIntValue(row, 0);
        int month = getIntValue(row, 1);
        BigDecimal amount = getDecimalValue(row, 2);
        BigDecimal units = getDecimalValue(row, 3);
        int transactions = getIntValue(row, 4);
        LocalDate generatedAt = getDateValue(row, 5);

        Optional<MonthlySaleReport> existing = monthlyRepo.findByReportYearAndReportMonth(year, month);
        MonthlySaleReport report = existing.orElse(new MonthlySaleReport());
        report.setReportYear(year);
        report.setReportMonth(month);
        report.setTotalSalesAmount(amount);
        report.setTotalUnitsSold(units);
        report.setTotalTransactions(transactions);
        report.setGeneratedAt(generatedAt != null ? generatedAt : LocalDate.now());
        monthlyRepo.save(report);

        return existing.isPresent() ? new Result(0, 1) : new Result(1, 0);
    }

    private Result importYearly(Row row) {
        int year = getIntValue(row, 0);
        BigDecimal amount = getDecimalValue(row, 1);
        BigDecimal units = getDecimalValue(row, 2);
        int transactions = getIntValue(row, 3);
        LocalDate generatedAt = getDateValue(row, 4);

        Optional<YearlySaleReport> existing = yearlyRepo.findByReportYear(year);
        YearlySaleReport report = existing.orElse(new YearlySaleReport());
        report.setReportYear(year);
        report.setTotalSalesAmount(amount);
        report.setTotalUnitsSold(units);
        report.setTotalTransactions(transactions);
        report.setGeneratedAt(generatedAt != null ? generatedAt : LocalDate.now());
        yearlyRepo.save(report);

        return existing.isPresent() ? new Result(0, 1) : new Result(1, 0);
    }

    private int getIntValue(Row row, int index) {
        Cell cell = row.getCell(index);
        return (cell != null && cell.getCellType() == CellType.NUMERIC) ? (int) cell.getNumericCellValue() : 0;
    }

    private BigDecimal getDecimalValue(Row row, int index) {
        Cell cell = row.getCell(index);
        return (cell != null && cell.getCellType() == CellType.NUMERIC)
            ? BigDecimal.valueOf(cell.getNumericCellValue())
            : BigDecimal.ZERO;
    }

    private LocalDate getDateValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return LocalDate.parse(cell.getStringCellValue(), DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            } catch (DateTimeParseException e) {
                log.warn("⚠️ Invalid date format in cell: {}", cell.getStringCellValue());
            }
        }
        return null;
    }

    public record ImportSummary(int created, int updated, List<String> errors) {}
    private record Result(int created, int updated) {}
}

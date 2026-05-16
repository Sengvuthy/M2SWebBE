package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.entity.ExpenseReport;
import SuperiorPro.SuperiorPOS.repository.ExpenseReportRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExpenseReportExportService {

    private final ExpenseReportRepository expenseReportRepository;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public int exportExpenseReportsToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("ExpenseReports");
        List<ExpenseReport> reports = expenseReportRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expense Reports");

            // 🔹 Header styling
            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);

            String[] headers = {
                "ID", "Expense ID", "Supplier Name", "Payer Name",
                "Expense Date", "Expense Time", "Product Name",
                "Expense Unit", "Expense Price", "Expense Amount", "Source"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (ExpenseReport report : reports) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(report.getId() != null ? report.getId() : 0);
                row.createCell(1).setCellValue(report.getExpenseId() != null ? report.getExpenseId() : "");
                row.createCell(2).setCellValue(report.getSupplierName() != null ? report.getSupplierName() : "");
                row.createCell(3).setCellValue(report.getPayerName() != null ? report.getPayerName() : "");
                row.createCell(4).setCellValue(report.getExpenseDate() != null ? report.getExpenseDate().toString() : "");
                row.createCell(5).setCellValue(report.getExpenseTime() != null ? report.getExpenseTime().toString() : "");
                row.createCell(6).setCellValue(report.getProductName() != null ? report.getProductName() : "");
                row.createCell(7).setCellValue(report.getExpenseUnit() != null ? report.getExpenseUnit().doubleValue() : 0);
                row.createCell(8).setCellValue(report.getExpensePrice() != null ? report.getExpensePrice().doubleValue() : 0.0);
                row.createCell(9).setCellValue(report.getExpenseAmount() != null ? report.getExpenseAmount().doubleValue() : 0.0);
                row.createCell(10).setCellValue(report.getSource() != null ? report.getSource() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("✅ Exported {} expense reports to Excel file: {}", reports.size(), filePath);
            return reports.size();
        }
    }

    /** Scheduled export every day at 2 AM */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional(readOnly = true)
    public void scheduledExport() {
        if (shuttingDown.get()) {
            log.info("⏹ Skipping scheduled export because application is shutting down");
            return;
        }
        try {
            int count = exportExpenseReportsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("ExpenseReports");
            log.info("🕒 Scheduled export completed: {} expense reports to {}", count, filePath);
        } catch (IOException e) {
            log.error("❌ Scheduled expense report export failed", e);
        }
    }

    @PreDestroy
    public void onShutdown() {
        log.info("⏹ Application is shutting down, stopping scheduled exports");
        shuttingDown.set(true);
    }
}

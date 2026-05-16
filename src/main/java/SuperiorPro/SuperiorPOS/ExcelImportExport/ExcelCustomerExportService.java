package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.entity.Customer;
import SuperiorPro.SuperiorPOS.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelCustomerExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelCustomerExportService.class);
    private final CustomerRepository customerRepository;

    public int exportCustomersToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("Customers");
        List<Customer> customers = customerRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Customers");

            // ✅ Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // ✅ Header row
            Row headerRow = sheet.createRow(0);
        	// ✅ Header row
            String[] headers = { "ID", "Name", "Phones", "Addresses", "Is Default", "Telegram ID" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ✅ Data rows
            int rowNum = 1;
            for (Customer customer : customers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(customer.getId() != null ? customer.getId() : 0);
                row.createCell(1).setCellValue(customer.getName() != null ? customer.getName() : "");

                // Join phones and addresses into comma-separated strings
                String phones = (customer.getPhones() != null && !customer.getPhones().isEmpty())
                        ? String.join(", ", customer.getPhones())
                        : "";
                String addresses = (customer.getAddresses() != null && !customer.getAddresses().isEmpty())
                        ? String.join(", ", customer.getAddresses())
                        : "";

                row.createCell(2).setCellValue(phones);
                row.createCell(3).setCellValue(addresses);
                row.createCell(4).setCellValue(Boolean.TRUE.equals(customer.getIsDefault()) ? "Yes" : "No");
                row.createCell(5).setCellValue(customer.getTelegramId() != null ? customer.getTelegramId() : 0);
            }

            // ✅ Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // ✅ Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("✅ Exported {} customers to Excel file: {}", customers.size(), filePath);
            return customers.size();
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledExport() {
        try {
            int count = exportCustomersToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Customers");
            log.info("📤 Scheduled export completed: {} customers to {}", count, filePath);
        } catch (IOException e) {
            log.error("❌ Scheduled export failed", e);
        }
    }
}

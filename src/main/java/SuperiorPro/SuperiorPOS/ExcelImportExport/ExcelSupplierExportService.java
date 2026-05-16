package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.entity.Supplier;
import SuperiorPro.SuperiorPOS.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelSupplierExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelSupplierExportService.class);
    private final SupplierRepository supplierRepository;

    public int exportSuppliersToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("Suppliers");
        List<Supplier> suppliers = supplierRepository.findByActiveTrue();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Suppliers");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Name", "Phone", "Email", "Address", "Is Default", "Active" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Supplier supplier : suppliers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(supplier.getId()); // ID
                row.createCell(1).setCellValue(supplier.getName() != null ? supplier.getName() : "");
                row.createCell(2).setCellValue(supplier.getPhone() != null ? supplier.getPhone() : "");
                row.createCell(3).setCellValue(supplier.getEmail() != null ? supplier.getEmail() : "");
                row.createCell(4).setCellValue(supplier.getAddress() != null ? supplier.getAddress() : "");
                row.createCell(5).setCellValue(Boolean.TRUE.equals(supplier.getIsDefault()) ? "Yes" : "No");
                row.createCell(6).setCellValue(Boolean.TRUE.equals(supplier.getActive()) ? "true" : "false");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("✅ Exported {} suppliers to Excel file: {}", suppliers.size(), filePath);
            return suppliers.size();
        }
    }

    // Scheduled export every day at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledExport() {
        try {
            int count = exportSuppliersToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Suppliers");
            log.info("📤 Scheduled export completed: {} suppliers to {}", count, filePath);
        } catch (IOException e) {
            log.error("❌ Scheduled export failed", e);
        }
    }
}

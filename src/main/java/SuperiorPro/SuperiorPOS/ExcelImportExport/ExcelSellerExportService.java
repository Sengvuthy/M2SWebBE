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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.entity.Seller;
import SuperiorPro.SuperiorPOS.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelSellerExportService {

    private final SellerRepository sellerRepository;

    public int exportSellersToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("Sellers");
        List<Seller> sellers = sellerRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sellers");

            // 🔹 Header setup
            String[] headers = { "ID", "Name", "Employee Code", "Phone" };
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 🔹 Data rows
            int rowNum = 1;
            for (Seller seller : sellers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(seller.getId() != null ? seller.getId() : 0);
                row.createCell(1).setCellValue(seller.getName() != null ? seller.getName() : "");
                row.createCell(2).setCellValue(seller.getEmployeeCode() != null ? seller.getEmployeeCode() : "");
                row.createCell(3).setCellValue(seller.getPhone() != null ? seller.getPhone() : "");
            }

            // 🔹 Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 🔹 Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("✅ Exported {} sellers to Excel file: {}", sellers.size(), filePath);
            return sellers.size();
        }
    }

    // Scheduled daily export at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledExport() {
        try {
            int count = exportSellersToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Sellers");
            log.info("🕒 Scheduled export completed: {} sellers to {}", count, filePath);
        } catch (IOException e) {
            log.error("❌ Scheduled seller export failed", e);
        }
    }
}

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
import SuperiorPro.SuperiorPOS.entity.Category;
import SuperiorPro.SuperiorPOS.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelCategoryExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelCategoryExportService.class);
    private final CategoryRepository categoryRepository;

    public int exportCategoriesToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("Categories");
        List<Category> categories = categoryRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Categories");

            // ✅ Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // ✅ Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Category ID", "Category Name"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ✅ Data rows
            int rowNum = 1;
            for (Category category : categories) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(category.getId());   // ID
                row.createCell(1).setCellValue(category.getName()); // Name
            }

            // ✅ Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // ✅ Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("📤 Exported {} categories to Excel file: {}", categories.size(), filePath);
            return categories.size();
        }
    }

    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void scheduledExport() {
        try {
            int count = exportCategoriesToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Categories");
            log.info("🕒 Scheduled export completed: {} categories to {}", count, filePath);
        } catch (IOException e) {
            log.error("❌ Scheduled export failed", e);
        }
    }
}

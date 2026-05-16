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
import SuperiorPro.SuperiorPOS.entity.ProductImport;
import SuperiorPro.SuperiorPOS.repository.ProductImportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelImportProductExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelImportProductExportService.class);
    private final ProductImportRepository importRepository;

    /**
     * Export all product imports to an Excel file.
     * @return number of records exported
     */
    public int exportProductImportsToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("ProductImports");
        List<ProductImport> imports = importRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Product Imports");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Import ID", "Barcode", "Product Name", "Khmer Name", "Import Unit",
                    "Buy Price", "Buy Amount", "Sale Price", "Import Date", "Import Time", "Importer Name"
                };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (ProductImport record : imports) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getId());
                row.createCell(1).setCellValue(record.getImportId());
                row.createCell(2).setCellValue(record.getBarcode());
                row.createCell(3).setCellValue(record.getProductName());
                row.createCell(4).setCellValue(record.getKhmerName());
                row.createCell(5).setCellValue(record.getImportUnit() != null ? record.getImportUnit().doubleValue() : 0);
                row.createCell(6).setCellValue(record.getBuyPrice() != null ? record.getBuyPrice().doubleValue() : 0.0);
                row.createCell(7).setCellValue(record.getBuyAmount() != null ? record.getBuyAmount().doubleValue() : 0.0);
                row.createCell(8).setCellValue(record.getSalePrice() != null ? record.getSalePrice().doubleValue() : 0.0);
                row.createCell(9).setCellValue(record.getImportDate() != null ? record.getImportDate().toString() : "");
                row.createCell(10).setCellValue(record.getImportTime() != null ? record.getImportTime().toString() : "");
                row.createCell(11).setCellValue(record.getImporterName());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info(" Exported {} product import records to Excel file: {}", imports.size(), filePath);
            return imports.size();
        }
    }

    /**
     * Scheduled daily export at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledExport() {
        try {
            int count = exportProductImportsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("ProductImports");
            log.info(" Scheduled product import export completed: {} records to {}", count, filePath);
        } catch (IOException e) {
            log.error("❌ Scheduled product import export failed", e);
        }
    }
}

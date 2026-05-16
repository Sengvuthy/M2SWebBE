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
import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelProductExportService {

    private final ProductRepository productRepository;

    public int exportProductsToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("Products");
        List<Product> products = productRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Products");

            // ✅ Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // ✅ Header row
            String[] headers = {
                "Barcode", "Name", "KhmerName", "Available Unit", "Buy Price", "Sale Price",
                "Category ID", "Category Name", "Supplier ID", "Supplier Name", "Image Path"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ✅ Data rows
            int rowNum = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getBarcode());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getKhmerName());
                row.createCell(3).setCellValue(product.getAvailableUnit() != null ? product.getAvailableUnit().doubleValue() : 0);
                row.createCell(4).setCellValue(product.getBuyPrice() != null ? product.getBuyPrice().doubleValue() : 0);
                row.createCell(5).setCellValue(product.getSalePrice() != null ? product.getSalePrice().doubleValue() : 0);
                row.createCell(6).setCellValue(product.getCategoryId() != null ? product.getCategoryId() : 0);
                row.createCell(7).setCellValue(product.getCategoryName() != null ? product.getCategoryName() : "");
                row.createCell(8).setCellValue(product.getSupplierId() != null ? product.getSupplierId() : 0);
                row.createCell(9).setCellValue(product.getSupplierName() != null ? product.getSupplierName() : "");
                row.createCell(10).setCellValue(product.getImagePath() != null ? product.getImagePath() : "");
            }

            // ✅ Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("✅ Exported {} products to Excel file: {}", products.size(), filePath);
            return products.size();
        }
    }

    // ✅ Scheduled export at 3 AM daily
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledExport() {
        try {
            int count = exportProductsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Products");
            log.info("📤 Scheduled product export completed: {} products to {}", count, filePath);
        } catch (IOException e) {
            log.error("❌ Scheduled product export failed", e);
        }
    }
}

package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.entity.Sale;
import SuperiorPro.SuperiorPOS.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelSaleExportService {

    private final SaleRepository saleRepository;

    public int exportSalesToExcel() throws IOException {
        // 🔹 Always overwrite the same file
        String filePath = ExcelPathResolver.resolveFixedPath("Sales");
        List<Sale> sales = saleRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sales");

            // 🔹 Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // 🔹 Header row (simplified: no Seller, no receive/change)
            String[] headers = {
                "Invoice", "Barcode", "Product", "Khmer Name",
                "Units Sold", "Unit Price", "Sold Amount",
                "Customer", "Sale Date", "Sale Time"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 🔹 Data rows
            int rowNum = 1;
            for (Sale sale : sales) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(safeString(sale.getInvoice()));
                row.createCell(1).setCellValue(safeString(sale.getBarcode()));
                row.createCell(2).setCellValue(safeString(sale.getProductName()));
                row.createCell(3).setCellValue(safeString(sale.getKhmerName()));
                row.createCell(4).setCellValue(sale.getNumberOfUnit().doubleValue());
                row.createCell(5).setCellValue(sale.getUnitPrice() != null ? sale.getUnitPrice().doubleValue() : 0);

                BigDecimal amount = sale.getSoldAmount() != null ? sale.getSoldAmount() : BigDecimal.ZERO;
                row.createCell(6).setCellValue(amount.doubleValue());

                row.createCell(7).setCellValue(safeString(sale.getCustomerName()));
                row.createCell(8).setCellValue(sale.getSaleDate() != null ? sale.getSaleDate().toString() : "");
                row.createCell(9).setCellValue(sale.getSaleTime() != null ? sale.getSaleTime().toString() : "");
            }

            // 🔹 Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 🔹 Write to file (overwrite)
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("✅ Exported {} sales to Excel: {}", sales.size(), filePath);
            return sales.size();
        }
    }

    // 🔹 Helper for null-safe strings
    private String safeString(String value) {
        return value != null ? value : "";
    }
}

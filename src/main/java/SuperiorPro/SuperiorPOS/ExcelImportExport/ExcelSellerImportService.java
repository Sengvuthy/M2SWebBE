package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.Seller;
import SuperiorPro.SuperiorPOS.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelSellerImportService {

    private final SellerRepository sellerRepository;

    public ImportSummary importSellersFromExcel(String filePath) throws IOException {
        if (!filePath.endsWith(".xlsx")) {
            throw new IOException("Invalid file format. Expected .xlsx");
        }

        int created = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("📥 Starting seller import from Excel: {}", filePath);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header

                String name = getCellValue(row, 1); // column 1 = Name
                if (name.isEmpty()) {
                    errors.add("❌ Empty seller name at row " + (row.getRowNum() + 1));
                    continue;
                }

                Long id = parseId(getCellValue(row, 0)); // column 0 = ID
                Seller seller = (id != null && sellerRepository.existsById(id))
                        ? sellerRepository.findById(id).orElse(new Seller())
                        : new Seller();

                seller.setName(name);
                seller.setEmployeeCode(getCellValue(row, 2)); // column 2 = Employee Code
                seller.setPhone(getCellValue(row, 3));        // column 3 = Phone

                sellerRepository.save(seller);

                if (id != null && seller.getId() != null) {
                    updated++;
                    log.info("🔄 Updated seller ID {}: {}", seller.getId(), seller.getName());
                } else {
                    created++;
                    log.info("✅ Created new seller: {}", seller.getName());
                }
            }

            log.info("📊 Seller import completed: {} updated, {} created, {} errors",
                     updated, created, errors.size());
            return new ImportSummary(created, updated, errors);
        }
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return "";
    }

    private Long parseId(String value) {
        try {
            return (value != null && !value.isBlank()) ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<String> previewSellerNames() {
        return sellerRepository.findAll().stream()
            .map(Seller::getName)
            .toList();
    }

    public record ImportSummary(int created, int updated, List<String> errors) {}
}

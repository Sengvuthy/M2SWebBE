package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.Supplier;
import SuperiorPro.SuperiorPOS.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelSupplierImportService {

    private final SupplierRepository supplierRepository;

    public ImportSummary importSuppliersFromExcel(String filePath) throws IOException {
        if (!filePath.endsWith(".xlsx")) {
            throw new IOException("Invalid file format. Please provide an .xlsx file.");
        }

        int createdCount = 0;
        int updatedCount = 0;
        List<String> errors = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("📥 Starting import from Excel file: {}", filePath);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String idRaw = getCellValue(row, 0);
                String name = getCellValue(row, 1);
                String phone = getCellValue(row, 2);
                String email = getCellValue(row, 3);
                String address = getCellValue(row, 4);
                String isDefaultRaw = getCellValue(row, 5);
                String activeRaw = getCellValue(row, 6); // ✅ Active column

                if (name.isBlank()) {
                    String msg = "⚠️ Skipped row " + (row.getRowNum() + 1) + ": Missing supplier name";
                    log.warn(msg);
                    errors.add(msg);
                    continue;
                }

                boolean isDefault = "yes".equalsIgnoreCase(isDefaultRaw.trim());
                boolean active = "true".equalsIgnoreCase(activeRaw.trim());

                try {
                    Supplier supplier = null;

                    // ✅ Try update by ID if provided
                    if (!idRaw.isBlank()) {
                        try {
                            Long id = Long.parseLong(idRaw);
                            supplier = supplierRepository.findById(id).orElse(null);
                        } catch (NumberFormatException e) {
                            errors.add("Row " + (row.getRowNum() + 1) + ": Invalid ID format");
                        }
                    }

                    if (supplier != null) {
                        supplier.setName(name.trim());
                        supplier.setPhone(phone);
                        supplier.setEmail(email == null || email.isBlank() ? null : email);
                        supplier.setAddress(address);
                        supplier.setIsDefault(isDefault);
                        supplier.setActive(active); // ✅ overwrite active from Excel
                        supplierRepository.save(supplier);
                        updatedCount++;
                        log.info("🔄 Updated supplier ID {} at row {}", supplier.getId(), row.getRowNum() + 1);
                    } else {
                        Supplier newSupplier = new Supplier();
                        newSupplier.setName(name.trim());
                        newSupplier.setPhone(phone);
                        newSupplier.setEmail(email == null || email.isBlank() ? null : email);
                        newSupplier.setAddress(address);
                        newSupplier.setIsDefault(isDefault);
                        newSupplier.setActive(active); // ✅ set active from Excel
                        supplierRepository.save(newSupplier);
                        createdCount++;
                        log.info("✅ Created new supplier '{}' at row {}", name, row.getRowNum() + 1);
                    }
                } catch (Exception e) {
                    String msg = "❌ Failed to process row " + (row.getRowNum() + 1) + ": " + e.getMessage();
                    log.error(msg, e);
                    errors.add(msg);
                }
            }

            log.info("📊 Import completed: {} updated, {} created, {} errors.", updatedCount, createdCount, errors.size());
            return new ImportSummary(createdCount, updatedCount, errors);
        } catch (Exception e) {
            throw new IOException("❌ Import failed: " + e.getMessage(), e);
        }
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> evaluateFormula(cell);
            default -> "";
        };
    }

    private String evaluateFormula(Cell cell) {
        try {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook()
                    .getCreationHelper().createFormulaEvaluator();
            CellValue value = evaluator.evaluate(cell);
            return switch (value.getCellType()) {
                case STRING -> value.getStringValue().trim();
                case NUMERIC -> String.valueOf((long) value.getNumberValue());
                case BOOLEAN -> String.valueOf(value.getBooleanValue());
                default -> "";
            };
        } catch (Exception e) {
            log.warn("⚠️ Failed to evaluate formula at row {} col {}: {}", 
                     cell.getRowIndex() + 1, cell.getColumnIndex(), e.getMessage());
            return "";
        }
    }

    public List<String> previewSupplierNames() {
        return supplierRepository.findAll().stream()
            .map(Supplier::getName)
            .toList();
    }

    public record ImportSummary(int created, int updated, List<String> errors) {}
}

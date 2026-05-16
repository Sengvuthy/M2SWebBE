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

import SuperiorPro.SuperiorPOS.entity.Permission;
import SuperiorPro.SuperiorPOS.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelPermissionImportService {

    private final PermissionRepository permissionRepository;

    public ImportSummary importPermissionsFromExcel(String filePath) throws IOException {
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
                if (row.getRowNum() == 0) continue; // skip header

                Long id = parseId(getCellValue(row, 0)); // column 0 = ID
                String name = getCellValue(row, 1);      // column 1 = Name
                String description = getCellValue(row, 2); // column 2 = Description

                if (name.isEmpty()) {
                    errors.add("❌ Empty permission name at row " + (row.getRowNum() + 1));
                    continue;
                }

                Permission permission;
                if (id != null && permissionRepository.existsById(id)) {
                    permission = permissionRepository.findById(id).orElse(new Permission());
                    updatedCount++;
                    log.info("🔄 Updating permission ID {} with name '{}'", id, name);
                } else {
                    permission = new Permission();
                    createdCount++;
                    log.info("✅ Creating new permission with name '{}'", name);
                }

                permission.setPermissionName(name);
                permission.setDescription(description);
                permissionRepository.save(permission);
            }

            log.info("📊 Import completed: {} updated, {} created, {} errors", updatedCount, createdCount, errors.size());
            return new ImportSummary(createdCount, updatedCount, errors);
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

    public List<String> previewPermissionNames() {
        return permissionRepository.findAll().stream()
            .map(Permission::getPermissionName)
            .toList();
    }

    public record ImportSummary(int created, int updated, List<String> errors) {}
}

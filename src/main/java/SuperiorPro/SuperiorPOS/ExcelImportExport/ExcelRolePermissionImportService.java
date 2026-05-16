package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.RolePermission;
import SuperiorPro.SuperiorPOS.entity.RolePermissionKey;
import SuperiorPro.SuperiorPOS.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelRolePermissionImportService {

    private final RolePermissionRepository rolePermissionRepository;

    public ImportSummary importRolesPermissionFromExcel(String filePath) throws IOException {
        if (!filePath.endsWith(".xlsx")) {
            throw new IOException("Invalid file format. Please provide an .xlsx file.");
        }

        int createdCount = 0;
        int updatedCount = 0;
        List<String> errors = new ArrayList<>();

        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("📥 Starting import from Excel file: {}", filePath);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Long roleId = getLongCellValue(row, 0);
                Long permissionId = getLongCellValue(row, 2);
                String roleName = getStringCellValue(row, 1);
                String permissionName = getStringCellValue(row, 3);

                if (roleId == null || permissionId == null) {
                    errors.add("❌ Invalid role or permission ID at row " + (row.getRowNum() + 1));
                    continue;
                }

                RolePermissionKey key = new RolePermissionKey(roleId, permissionId);
                Optional<RolePermission> existing = rolePermissionRepository.findById(key);

                if (existing.isPresent()) {
                    RolePermission rp = existing.get();
                    rp.setRoleName(roleName);
                    rp.setPermissionName(permissionName);
                    rolePermissionRepository.save(rp);
                    updatedCount++;
                    log.info("🔄 Updated mapping: roleId={}, permissionId={}", roleId, permissionId);
                } else {
                    RolePermission rp = new RolePermission();
                    rp.setId(key);
                    rp.setRoleName(roleName);
                    rp.setPermissionName(permissionName);
                    rolePermissionRepository.save(rp);
                    createdCount++;
                    log.info("✅ Created new mapping: roleId={}, permissionId={}", roleId, permissionId);
                }
            }

            log.info("📊 Import completed: {} updated, {} created, {} errors", updatedCount, createdCount, errors.size());
            return new ImportSummary(createdCount, updatedCount, errors);
        }
    }

    private Long getLongCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        return (cell != null && cell.getCellType() == CellType.NUMERIC) ? (long) cell.getNumericCellValue() : null;
    }

    private String getStringCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        return (cell != null && cell.getCellType() == CellType.STRING) ? cell.getStringCellValue().trim() : "";
    }

    public record ImportSummary(int created, int updated, List<String> errors) {}
}

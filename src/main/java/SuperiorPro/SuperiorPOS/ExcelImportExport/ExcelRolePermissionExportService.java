package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.entity.RolePermission;
import SuperiorPro.SuperiorPOS.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelRolePermissionExportService {

    private final RolePermissionRepository rolePermissionRepository;

    public int exportRolePermissionsToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("RolePermissions");
        List<RolePermission> mappings = rolePermissionRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("RolePermissions");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Role ID");
            headerRow.createCell(1).setCellValue("Role Name");
            headerRow.createCell(2).setCellValue("Permission ID");
            headerRow.createCell(3).setCellValue("Permission Name");

            for (int i = 0; i < 4; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (RolePermission rp : mappings) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rp.getId().getRoleId());
                row.createCell(1).setCellValue(rp.getRoleName());
                row.createCell(2).setCellValue(rp.getId().getPermissionId());
                row.createCell(3).setCellValue(rp.getPermissionName());
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("Exported {} role-permission mappings to Excel file: {}", mappings.size(), filePath);
            return mappings.size();
        }
    }
}

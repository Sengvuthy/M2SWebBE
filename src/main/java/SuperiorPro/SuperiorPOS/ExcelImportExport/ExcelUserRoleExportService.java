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
import SuperiorPro.SuperiorPOS.entity.UserRole;
import SuperiorPro.SuperiorPOS.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelUserRoleExportService {

    private final UserRoleRepository userRoleRepository;

    public int exportUserRolesToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("UsersRole");
        List<UserRole> mappings = userRoleRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("UserRoles");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("User ID");
            headerRow.createCell(1).setCellValue("Username");
            headerRow.createCell(2).setCellValue("Role ID");
            headerRow.createCell(3).setCellValue("Role Name");

            for (int i = 0; i < 4; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (UserRole ur : mappings) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(ur.getId().getUserId());
                row.createCell(1).setCellValue(ur.getUserName());
                row.createCell(2).setCellValue(ur.getId().getRoleId());
                row.createCell(3).setCellValue(ur.getRoleName());
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("✅ Exported {} user-role mappings to Excel: {}", mappings.size(), filePath);
            return mappings.size();
        }
    }
}

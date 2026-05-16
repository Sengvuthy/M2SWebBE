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
import SuperiorPro.SuperiorPOS.entity.Role;
import SuperiorPro.SuperiorPOS.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelRoleExportService {

    private final RoleRepository roleRepository;

    public int exportRolesToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("Roles");
        List<Role> roles = roleRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Roles");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Role Name", "Description" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Role role : roles) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(role.getId() != null ? role.getId() : 0);
                row.createCell(1).setCellValue(role.getRoleName() != null ? role.getRoleName() : "");
                row.createCell(2).setCellValue(role.getDescription() != null ? role.getDescription() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("✅ Exported {} roles to Excel file: {}", roles.size(), filePath);
            return roles.size();
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledExport() {
        try {
            int count = exportRolesToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Roles");
            log.info("🕒 Scheduled export completed: {} roles to {}", count, filePath);
        } catch (IOException e) {
            log.error("❌ Scheduled export failed", e);
        }
    }
}

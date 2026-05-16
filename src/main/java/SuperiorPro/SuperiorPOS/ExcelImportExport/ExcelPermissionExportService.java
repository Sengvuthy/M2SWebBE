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
import SuperiorPro.SuperiorPOS.entity.Permission;
import SuperiorPro.SuperiorPOS.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelPermissionExportService {

    private final PermissionRepository permissionRepository;

    public int exportPermissionsToExcel() throws IOException {
        String filePath = ExcelPathResolver.resolveFixedPath("Permissions");
        List<Permission> permissions = permissionRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Permissions");

            // Header styling
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Permission Name", "Description" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate rows
            int rowNum = 1;
            for (Permission permission : permissions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(permission.getId() != null ? permission.getId() : 0);
                row.createCell(1).setCellValue(permission.getPermissionName() != null ? permission.getPermissionName() : "");
                row.createCell(2).setCellValue(permission.getDescription() != null ? permission.getDescription() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            log.info("✅ Exported {} permissions to Excel file: {}", permissions.size(), filePath);
            return permissions.size();
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledExport() {
        try {
            int count = exportPermissionsToExcel();
            String filePath = ExcelPathResolver.resolveFixedPath("Permissions");
            log.info("🕒 Scheduled export completed: {} permissions to {}", count, filePath);
        } catch (IOException e) {
            log.error("❌ Scheduled export failed", e);
        }
    }
}

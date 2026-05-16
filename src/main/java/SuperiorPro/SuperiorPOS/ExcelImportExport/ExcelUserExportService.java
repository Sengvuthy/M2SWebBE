package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.entity.User;
import SuperiorPro.SuperiorPOS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelUserExportService {

	private final UserRepository userRepository;

	public int exportUsersToExcel() throws IOException {
		String filePath = ExcelPathResolver.resolveFixedPath("Users");
		List<User> users = userRepository.findAll();

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Users");

			// ===== HEADER STYLE =====
			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			headerStyle.setFont(font);

			// ===== HEADER ROW =====
			Row header = sheet.createRow(0);
			String[] columns = { "Username", "Password (hashed)", "Phone Number" };

			for (int i = 0; i < columns.length; i++) {
				Cell cell = header.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerStyle);
			}

			// ===== DATA ROWS =====
			int rowNum = 1;
			for (User user : users) {
				Row row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue(user.getUserName());
				row.createCell(1).setCellValue(user.getPassword());
				row.createCell(2).setCellValue(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
			}

			// Auto-size all columns
			for (int i = 0; i < columns.length; i++) {
				sheet.autoSizeColumn(i);
			}

			// ===== WRITE FILE =====
			try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
				workbook.write(fileOut);
			}

			log.info("✅ Exported {} users to Excel: {}", users.size(), filePath);
			return users.size();
		}
	}

	// Scheduled export at 2 AM daily
	@Scheduled(cron = "0 0 2 * * *")
	public void scheduledExport() {
		try {
			int count = exportUsersToExcel();
			String filePath = ExcelPathResolver.resolveFixedPath("Users");
			log.info("🕒 Scheduled export completed: {} users to {}", count, filePath);
		} catch (IOException e) {
			log.error("❌ Scheduled export failed", e);
		}
	}
}

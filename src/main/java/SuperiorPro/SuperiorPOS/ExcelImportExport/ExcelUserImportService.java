package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.User;
import SuperiorPro.SuperiorPOS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelUserImportService {

	private final UserRepository userRepository;

	public ImportSummary importUsersFromExcel(String filePath) throws IOException {
		if (!filePath.endsWith(".xlsx")) {
			throw new IOException("Invalid file format. Expected .xlsx");
		}

		int created = 0;
		int updated = 0;
		List<String> errors = new ArrayList<>();

		try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheetAt(0);
			log.info("📥 Starting import from Excel: {}", filePath);

			for (Row row : sheet) {
				if (row.getRowNum() == 0)
					continue; // Skip header

				String username = getCellValue(row, 0);
				String password = getCellValue(row, 1);
				String phoneNumber = getCellValue(row, 2);

				if (username.isEmpty()) {
					errors.add("❌ Empty username at row " + (row.getRowNum() + 1));
					continue;
				}

				if (password.isEmpty()) {
					password = "default"; // fallback if missing
				}

				Optional<User> existing = userRepository.findByUserName(username);
				if (existing.isPresent()) {
					User user = existing.get();
					user.setPassword(password);
					user.setPhoneNumber(phoneNumber);
					userRepository.save(user);
					updated++;
					log.info("🔄 Updated user: {}", username);
				} else {
					User user = new User();
					user.setUserName(username);
					user.setPassword(password);
					user.setPhoneNumber(phoneNumber);
					userRepository.save(user);
					created++;
					log.info("✅ Created new user: {}", username);
				}
			}

			log.info("📊 Import completed: {} updated, {} created, {} errors", updated, created, errors.size());
			return new ImportSummary(created, updated, errors);
		}
	}

	private String getCellValue(Row row, int index) {
		Cell cell = row.getCell(index);
		if (cell == null)
			return "";
		return switch (cell.getCellType()) {
		case STRING -> cell.getStringCellValue().trim();
		case NUMERIC -> String.valueOf((long) cell.getNumericCellValue()); // for phone numbers
		default -> "";
		};
	}

	public List<String> previewUsernames() {
		return userRepository.findAll().stream().map(User::getUserName).toList();
	}

	public record ImportSummary(int created, int updated, List<String> errors) {
	}
}

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

import SuperiorPro.SuperiorPOS.entity.UserRole;
import SuperiorPro.SuperiorPOS.entity.UserRoleKey;
import SuperiorPro.SuperiorPOS.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelUserRoleImportService {

	private final UserRoleRepository userRoleRepository;

	public ImportSummary importUserRolesFromExcel(String filePath) throws IOException {
		if (!filePath.endsWith(".xlsx")) {
			throw new IOException("Invalid file format. Expected .xlsx");
		}

		int createdCount = 0;
		int updatedCount = 0;
		List<String> errors = new ArrayList<>();

		try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheetAt(0);
			log.info("📥 Starting import from Excel: {}", filePath);

			for (Row row : sheet) {
				if (row.getRowNum() == 0)
					continue; // skip header

				Long userId = getLongCellValue(row, 0);
				String userName = getStringCellValue(row, 1);
				Long roleId = getLongCellValue(row, 2);
				String roleName = getStringCellValue(row, 3);

				// ✅ Validate IDs before proceeding
				if (userId == null || roleId == null) {
					String msg = "❌ Skipped row " + (row.getRowNum() + 1) + ": Missing or invalid userId/roleId";
					log.warn(msg);
					errors.add(msg);
					continue;
				}

				UserRoleKey key = new UserRoleKey(userId, roleId);
				Optional<UserRole> existing = userRoleRepository.findById(key);

				if (existing.isPresent()) {
					UserRole ur = existing.get();
					ur.setUserName(userName);
					ur.setRoleName(roleName);
					userRoleRepository.save(ur);
					updatedCount++;
					log.info("🔄 Updated mapping: userId={}, roleId={}", userId, roleId);
				} else {
					UserRole ur = new UserRole();
					ur.setId(key);
					ur.setUserName(userName);
					ur.setRoleName(roleName);
					userRoleRepository.save(ur);
					createdCount++;
					log.info("✅ Created new mapping: userId={}, roleId={}", userId, roleId);
				}
			}

			log.info("📊 Import completed: {} updated, {} created, {} errors", updatedCount, createdCount,
					errors.size());
			return new ImportSummary(createdCount, updatedCount, errors);
		}
	}

	// ✅ Handles both numeric and string cells safely
	private Long getLongCellValue(Row row, int index) {
		Cell cell = row.getCell(index);
		if (cell == null)
			return null;

		if (cell.getCellType() == CellType.NUMERIC) {
			return (long) cell.getNumericCellValue();
		} else if (cell.getCellType() == CellType.STRING) {
			try {
				return Long.parseLong(cell.getStringCellValue().trim());
			} catch (NumberFormatException e) {
				log.warn("⚠️ Invalid numeric string at row {} col {}: {}", row.getRowNum() + 1, index,
						cell.getStringCellValue());
				return null;
			}
		}
		return null;
	}

	private String getStringCellValue(Row row, int index) {
		Cell cell = row.getCell(index);
		if (cell == null)
			return "";
		if (cell.getCellType() == CellType.STRING) {
			return cell.getStringCellValue().trim();
		} else if (cell.getCellType() == CellType.NUMERIC) {
			return String.valueOf((long) cell.getNumericCellValue());
		}
		return "";
	}

	public record ImportSummary(int created, int updated, List<String> errors) {
	}
}

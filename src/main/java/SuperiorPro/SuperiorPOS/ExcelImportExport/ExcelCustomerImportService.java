package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.Customer;
import SuperiorPro.SuperiorPOS.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelCustomerImportService {

	private final CustomerRepository customerRepository;

	public ImportSummary importCustomersFromExcel(String filePath) throws IOException {
		if (!filePath.endsWith(".xlsx")) {
			throw new IOException("Invalid file format. Please provide an .xlsx file.");
		}

		int createdCount = 0;
		int updatedCount = 0;
		List<String> errors = new ArrayList<>();

		try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheetAt(0);
			log.info("📥 Starting import from Excel file: {}", filePath);

			for (Row row : sheet) {
				if (row.getRowNum() == 0)
					continue; // Skip header

				String idRaw = getCellValue(row, 0);
				String name = getCellValue(row, 1);
				String phonesRaw = getCellValue(row, 2);
				String addressesRaw = getCellValue(row, 3);
				String isDefaultRaw = getCellValue(row, 4);
				String telegramIdRaw = getCellValue(row, 5);

				if (name.isBlank()) {
					String msg = "⚠️ Skipped row " + (row.getRowNum() + 1) + ": Missing customer name";
					log.warn(msg);
					errors.add(msg);
					continue;
				}

				boolean isDefault = "yes".equalsIgnoreCase(isDefaultRaw.trim());

				// Split comma-separated values into lists
				List<String> phones = phonesRaw.isBlank() ? new ArrayList<>() : List.of(phonesRaw.split("\\s*,\\s*"));
				List<String> addresses = addressesRaw.isBlank() ? new ArrayList<>()
						: List.of(addressesRaw.split("\\s*,\\s*"));

				try {
					Customer customer = null;

					// ✅ Try update by ID if provided
					if (!idRaw.isBlank()) {
						try {
							Long id = Long.parseLong(idRaw);
							customer = customerRepository.findById(id).orElse(null);
						} catch (NumberFormatException e) {
							errors.add("Row " + (row.getRowNum() + 1) + ": Invalid ID format");
						}
					}

					if (customer != null) {
						customer.setName(name.trim());
						customer.setPhones(phones);
						customer.setAddresses(addresses);
						customer.setIsDefault(isDefault);

						Long telegramId = telegramIdRaw.isBlank() ? null : Long.parseLong(telegramIdRaw);
						customer.setTelegramId(telegramId);

						customerRepository.save(customer);
						updatedCount++;
					} else {
						Customer newCustomer = new Customer();
						newCustomer.setName(name.trim());
						newCustomer.setPhones(phones);
						newCustomer.setAddresses(addresses);
						newCustomer.setIsDefault(isDefault);

						Long telegramId = telegramIdRaw.isBlank() ? null : Long.parseLong(telegramIdRaw);

						newCustomer.setTelegramId(telegramId);

						customerRepository.save(newCustomer);
						createdCount++;
					}
				} catch (Exception e) {
					String msg = "❌ Failed to process row " + (row.getRowNum() + 1) + ": " + e.getMessage();
					log.error(msg, e);
					errors.add(msg);
				}
			}

			log.info("📊 Import completed: {} updated, {} created, {} errors.", updatedCount, createdCount,
					errors.size());
			return new ImportSummary(createdCount, updatedCount, errors);
		} catch (Exception e) {
			throw new IOException("❌ Import failed: " + e.getMessage(), e);
		}
	}

	private String getCellValue(Row row, int index) {
		Cell cell = row.getCell(index);
		if (cell == null)
			return "";

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
			FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
			CellValue value = evaluator.evaluate(cell);
			return switch (value.getCellType()) {
			case STRING -> value.getStringValue().trim();
			case NUMERIC -> String.valueOf((long) value.getNumberValue());
			case BOOLEAN -> String.valueOf(value.getBooleanValue());
			default -> "";
			};
		} catch (Exception e) {
			log.warn("⚠️ Failed to evaluate formula at row {} col {}: {}", cell.getRowIndex() + 1,
					cell.getColumnIndex(), e.getMessage());
			return "";
		}
	}

	public List<String> previewCustomerNames() {
		return customerRepository.findAll().stream().map(Customer::getName).toList();
	}

	public record ImportSummary(int created, int updated, List<String> errors) {
	}
}

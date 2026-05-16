package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.Category;
import SuperiorPro.SuperiorPOS.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelCategoryImportService {

    private final CategoryRepository categoryRepository;

    public ImportSummary importCategoriesFromExcel(String filePath) throws IOException {
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
                if (row.getRowNum() == 0) continue; // Skip header

                int excelRowNum = row.getRowNum() + 1;
                String name = getCellValue(row, 1);

                if (name.isEmpty()) {
                    String error = "Row " + excelRowNum + ": Category name is empty";
                    errors.add(error);
                    log.warn("❌ {}", error);
                    continue;
                }

                try {
                    List<Category> matches = categoryRepository.findByNameIgnoreCase(name.trim());

                    if (matches.size() > 1) {
                        String error = "Row " + excelRowNum + ": Multiple categories found for '" + name + "'";
                        errors.add(error);
                        log.warn("❌ {}", error);
                        continue;
                    }

                    if (matches.size() == 1) {
                        Category category = matches.get(0);
                        category.setName(name.trim()); // optional update
                        categoryRepository.save(category);
                        updatedCount++;
                        log.info("🔄 Updated existing category: {}", name);
                    } else {
                        Category category = new Category();
                        category.setName(name.trim());
                        categoryRepository.save(category);
                        createdCount++;
                        log.info("✅ Created new category: {}", name);
                    }
                } catch (Exception e) {
                    String error = "Row " + excelRowNum + ": " + e.getMessage();
                    errors.add(error);
                    log.warn("⚠️ Failed to import row {}: {}", excelRowNum, e.getMessage());
                }
            }

            log.info("📊 Import completed: {} updated, {} created, {} errors",
                    updatedCount, createdCount, errors.size());

            return new ImportSummary(createdCount, updatedCount, errors);
        }
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    public List<String> previewCategoryNames() {
        return categoryRepository.findAll().stream()
            .map(Category::getName)
            .collect(Collectors.toList());
    }

    // ✅ ImportSummary now includes errors
    public record ImportSummary(int created, int updated, List<String> errors) {}
}

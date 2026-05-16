package SuperiorPro.SuperiorPOS.ExcelController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleReportExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleReportImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleReportImportService.ImportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/excel/sale-reports")
@RequiredArgsConstructor
public class ExcelSaleReportController {

	private final ExcelSaleReportExportService exportService;
	private final ExcelSaleReportImportService importService;

	// 🔹 Export Sale Reports to Excel (manual trigger)
	@GetMapping("/export")
	public ResponseEntity<String> exportSaleReports() {
		try {
			String filePath = exportService.exportSaleReportsToExcel();
			log.info("📤 Exported sale reports to {}", filePath);
			return ResponseEntity.ok("✅ Exported sale reports to: " + filePath);
		} catch (IOException e) {
			log.error("❌ Export failed", e);
			return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
		}
	}
	
	@GetMapping("/export/monthly")
	public ResponseEntity<String> exportMonthlyReports() {
	    try {
	        String filePath = exportService.exportMonthlyReportsToExcel();
	        return ResponseEntity.ok("✅ Exported monthly reports to: " + filePath);
	    } catch (IOException e) {
	        log.error("❌ Monthly export failed", e);
	        return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
	    }
	}

	// 🔹 Import Sale Reports from latest Excel file
	@PostMapping("/import")
	public ResponseEntity<String> importSaleReports() {
		try {
			String filePath = exportService.getLatestDailyReportPath();
			ImportSummary summary = importService.importSaleReportsFromExcel(filePath);

			StringBuilder response = new StringBuilder().append("✅ Imported sale reports from: ").append(filePath)
					.append("\n").append("🔄 ").append(summary.updated()).append(" updated\n").append("✅ ")
					.append(summary.created()).append(" created\n");

			if (!summary.errors().isEmpty()) {
				response.append("❌ ").append(summary.errors().size()).append(" errors:\n");
				summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
			}

			log.info("📊 Import summary: {} updated, {} created, {} errors", summary.updated(), summary.created(),
					summary.errors().size());

			return ResponseEntity.ok(response.toString());
		} catch (IOException e) {
			log.error("❌ Import failed", e);
			return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
		}
	}
	
	@PostMapping("/import/monthly")
	public ResponseEntity<ImportSummary> importMonthlyReports() {
	    try {
	        String filePath = "C:\\ExcelExportImport\\SaleReports\\MonthlyReport\\MonthlyReport "
	            + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + ".xlsx";

	        ImportSummary summary = importService.importSaleReportsFromExcel(filePath);

	        return ResponseEntity.ok(summary);
	    } catch (IOException e) {
	        log.error("❌ Monthly import failed", e);
	        return ResponseEntity.internalServerError()
	            .body(new ImportSummary(0, 0, List.of("❌ Import failed: " + e.getMessage())));
	    }
	}

	// 🔹 Preview Sale Report Types
	@GetMapping("/preview")
	public ResponseEntity<List<String>> previewSaleReportTypes() {
		List<String> preview = List.of("Daily Reports", "Monthly Reports", "Yearly Reports");
		return ResponseEntity.ok(preview);
	}
}

package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.DTO.MonthlyReportDTO;
import SuperiorPro.SuperiorPOS.DTO.SaleReportDTO;
import SuperiorPro.SuperiorPOS.DTO.YearlyReportDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleReportExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleReportImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleReportImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.entity.SaleReport;
import SuperiorPro.SuperiorPOS.mapper.SaleReportMapper;
import SuperiorPro.SuperiorPOS.repository.SaleReportRepository;
import SuperiorPro.SuperiorPOS.service.SaleReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sale_report")
public class SaleReportController {

	private final SaleReportService saleReportService;
	private final SaleReportRepository saleReportRepository;
	private final ExcelSaleReportExportService excelSaleReportExportService;
	private final ExcelSaleReportImportService excelSaleReportImportService;

	// 🔹 Save Daily Report
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@PostMapping("/report/daily")
	public ResponseEntity<SaleReportDTO> saveDailyReport(@RequestBody SaleReportDTO dto) {
		SaleReport report = saleReportRepository.findByReportDate(dto.getReportDate()).orElse(new SaleReport());

		report.setReportDate(dto.getReportDate());
		report.setTotalSalesAmount(dto.getTotalSalesAmount());
		report.setTotalUnitsSold(dto.getTotalUnitsSold());
		report.setTotalTransactions(dto.getTotalTransactions());

		saleReportRepository.save(report);
		log.info("✅ Daily report saved for {}", dto.getReportDate());

		return ResponseEntity.ok(SaleReportMapper.toDTO(report));
	}

	// 🔹 Fetch Daily Report
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/report/daily")
	public ResponseEntity<SaleReportDTO> getDailyReport(@RequestParam LocalDate date) {
		return saleReportRepository.findByReportDate(date)
				.map(report -> ResponseEntity.ok(SaleReportMapper.toDTO(report)))
				.orElse(ResponseEntity.notFound().build());
	}

	// 🔹 Import Daily Report from Excel
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@PostMapping("/import/daily")
	public ResponseEntity<?> importDailyReport() {
		try {
			String filePath = excelSaleReportExportService.getLatestDailyReportPath();
			ImportSummary summary = excelSaleReportImportService.importSaleReportsFromExcel(filePath);

			log.info("📥 Import summary: {} updated, {} created, {} errors", summary.updated(), summary.created(),
					summary.errors().size());

			return ResponseEntity.ok(Map.of("status", "success", "path", filePath, "updated", summary.updated(),
					"created", summary.created(), "errors", summary.errors()));
		} catch (IOException e) {
			log.error("❌ Import failed", e);
			return ResponseEntity.internalServerError()
					.body(Map.of("status", "error", "message", "❌ Import failed: " + e.getMessage()));
		}
	}

	// 🔹 Generate Monthly Report (DTO with daily breakdown + persistence)
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/report/monthly")
	public ResponseEntity<?> generateMonthlyReport(@RequestParam int year, @RequestParam int month) {
		if (month < 1 || month > 12) {
			return ResponseEntity.badRequest()
					.body(Map.of("status", "error", "message", "❌ Month must be between 1 and 12"));
		}
		if (year < 2000 || year > LocalDate.now().getYear()) {
			return ResponseEntity.badRequest()
					.body(Map.of("status", "error", "message", "❌ Year must be >= 2000 and <= current year"));
		}

		log.info("▶ Request received: generate monthly report {}/{}", month, year);
		MonthlyReportDTO dto = saleReportService.generateMonthlyReport(year, month);
		log.info("✔ Monthly report persisted and returned for {}/{}", month, year);

		return ResponseEntity.ok(dto);
	}

	// 🔹 Export Monthly Reports to Excel
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/export/monthly")
	public ResponseEntity<?> exportMonthlyReports() {
		try {
			String filePath = excelSaleReportExportService.exportMonthlyReportsToExcel();
			log.info("📤 Monthly reports exported to {}", filePath);

			return ResponseEntity.ok(Map.of("status", "success", "path", filePath, "message",
					"✅ Monthly reports exported successfully"));
		} catch (IOException e) {
			log.error("❌ Monthly export failed", e);
			return ResponseEntity.internalServerError()
					.body(Map.of("status", "error", "message", "❌ Export failed: " + e.getMessage()));
		}
	}

	// 🔹 Generate Yearly Report (DTO with monthly breakdown + persistence)
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/report/yearly")
	public ResponseEntity<?> generateYearlyReport(@RequestParam int year) {
		if (year < 2000 || year > LocalDate.now().getYear()) {
			return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "❌ Invalid year"));
		}

		log.info("▶ Request received: generate yearly report {}", year);

		// 🔹 First persist yearly summary
		saleReportService.generateYearlyReport(year);

		// 🔹 Then fetch DTO from DB
		YearlyReportDTO dto = saleReportService.getYearlyReportDTO(year);

		return ResponseEntity.ok(dto);
	}

	// 🔹 Import Yearly Report from Excel
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@PostMapping("/import/yearly")
	public ResponseEntity<?> importYearlyReports() {
		try {
			String filePath = excelSaleReportExportService.getLatestYearlyReportPath();
			ImportSummary summary = excelSaleReportImportService.importSaleReportsFromExcel(filePath);

			log.info("📥 Yearly import summary: {} created, {} updated, {} errors", summary.created(),
					summary.updated(), summary.errors().size());

			return ResponseEntity.ok(Map.of("status", "success", "path", filePath, "created", summary.created(),
					"updated", summary.updated(), "errors", summary.errors()));
		} catch (IOException e) {
			log.error("❌ Yearly import failed", e);
			return ResponseEntity.internalServerError()
					.body(Map.of("status", "error", "message", "❌ Import failed: " + e.getMessage()));
		}
	}

	// 🔹 Export Yearly Reports to Excel
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/export/yearly")
	public ResponseEntity<?> exportYearlyReports() {
		try {
			String filePath = excelSaleReportExportService.exportYearlyReportsToExcel();
			log.info("📤 Yearly reports exported to {}", filePath);

			return ResponseEntity.ok(
					Map.of("status", "success", "path", filePath, "message", "✅ Yearly reports exported successfully"));
		} catch (IOException e) {
			log.error("❌ Yearly export failed", e);
			return ResponseEntity.internalServerError()
					.body(Map.of("status", "error", "message", "❌ Export failed: " + e.getMessage()));
		}
	}
}

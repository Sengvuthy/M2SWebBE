package SuperiorPro.SuperiorPOS.ExcelImportExport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.MonthlySaleReport;
import SuperiorPro.SuperiorPOS.entity.SaleReport;
import SuperiorPro.SuperiorPOS.entity.YearlySaleReport;
import SuperiorPro.SuperiorPOS.repository.MonthlySaleReportRepository;
import SuperiorPro.SuperiorPOS.repository.SaleReportRepository;
import SuperiorPro.SuperiorPOS.repository.YearlySaleReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelSaleReportExportService {

	private final SaleReportRepository saleReportRepository;
	private final MonthlySaleReportRepository monthlyReportRepository;
	private final YearlySaleReportRepository yearlyReportRepository;

	// 🔹 Build dynamic file path: Income dd-MMM-yyyy.xlsx
	private String buildDailyReportPath() {
		String folder = "C:\\ExcelExportImport\\SaleReports\\DailyReport";
		File dir = new File(folder);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String dateStr = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		return folder + "\\Income " + dateStr + ".xlsx";
	}

	// 🔹 Export all sale reports to Excel
	public String exportSaleReportsToExcel() throws IOException {
		String folder = "C:\\ExcelExportImport\\SaleReports\\DailyReport";
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdirs();

		String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		String filePath = folder + "\\Income " + dateStr + ".xlsx";

		List<SaleReport> reports = saleReportRepository.findAll();

		try (Workbook workbook = new XSSFWorkbook()) {
			if (!reports.isEmpty()) {
				writeDailySheet(workbook, reports);
			} else {
				log.warn("⚠️ No sale reports found to export");
			}

			try (FileOutputStream out = new FileOutputStream(filePath)) {
				workbook.write(out);
			}
		}

		log.info("✅ Exported {} sale reports to {}", reports.size(), filePath);
		return filePath;
	}

	private void writeDailySheet(Workbook workbook, List<SaleReport> reports) {
		Sheet sheet = workbook.createSheet("Daily Reports");
		String[] headers = { "Date", "Total Amount", "Units Sold", "Transactions" };
		Row header = sheet.createRow(0);
		applyHeaders(header, headers, workbook);

		int rowNum = 1;
		for (SaleReport r : reports) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(r.getReportDate().toString());
			row.createCell(1).setCellValue(r.getTotalSalesAmount().doubleValue());
			row.createCell(2).setCellValue(r.getTotalUnitsSold().doubleValue());
			row.createCell(3).setCellValue(r.getTotalTransactions());
		}
		autoSize(sheet, headers.length);
	}

	// 🔹 Export monthly reports
	public String exportMonthlyReportsToExcel() throws IOException {
		String folder = "C:\\ExcelExportImport\\SaleReports\\MonthlyReport";
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdirs();

		String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		String filePath = folder + "\\MonthlyReport " + dateStr + ".xlsx";

		List<MonthlySaleReport> reports = monthlyReportRepository.findAll();

		try (Workbook workbook = new XSSFWorkbook()) {
			if (!reports.isEmpty()) {
				writeMonthlySheet(workbook, reports);
			} else {
				log.warn("⚠️ No monthly reports found to export");
			}

			try (FileOutputStream out = new FileOutputStream(filePath)) {
				workbook.write(out);
			}
		}

		log.info("✅ Exported {} monthly reports to {}", reports.size(), filePath);
		return filePath;
	}

	private void writeMonthlySheet(Workbook workbook, List<MonthlySaleReport> reports) {
		Sheet sheet = workbook.createSheet("Monthly Reports");
		String[] headers = { "Year", "Month", "Total Amount", "Units Sold", "Transactions" };
		Row header = sheet.createRow(0);
		applyHeaders(header, headers, workbook);

		int rowNum = 1;
		for (MonthlySaleReport r : reports) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(r.getReportYear());
			row.createCell(1).setCellValue(r.getReportMonth());
			row.createCell(2).setCellValue(r.getTotalSalesAmount().doubleValue());
			row.createCell(3).setCellValue(r.getTotalUnitsSold().doubleValue());
			row.createCell(4).setCellValue(r.getTotalTransactions());
		}
		autoSize(sheet, headers.length);
	}
	
	// 🔹 Helper for import controller to get latest yearly file path
	public String getLatestYearlyReportPath() {
	    String folder = "C:\\ExcelExportImport\\SaleReports\\YearlyReport";
	    File dir = new File(folder);
	    if (!dir.exists()) {
	        dir.mkdirs();
	    }
	    String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
	    return folder + "\\YearlyReport " + dateStr + ".xlsx";
	}

	public String exportYearlyReportsToExcel() throws IOException {
		String folder = "C:\\ExcelExportImport\\SaleReports\\YearlyReport";
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdirs();
		String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		String filePath = folder + "\\YearlyReport " + dateStr + ".xlsx";
		List<YearlySaleReport> reports = yearlyReportRepository.findAll();
		try (Workbook workbook = new XSSFWorkbook()) {
			if (!reports.isEmpty()) {
				writeYearlySheet(workbook, reports);
			} else {
				log.warn("⚠️ No yearly reports found to export");
			}
			try (FileOutputStream out = new FileOutputStream(filePath)) {
				workbook.write(out);
			}
		}
		log.info("✅ Exported {} yearly reports to {}", reports.size(), filePath);
		return filePath;
	}

	private void writeYearlySheet(Workbook workbook, List<YearlySaleReport> reports) {
		Sheet sheet = workbook.createSheet("Yearly Reports");
		String[] headers = { "Year", "Total Amount", "Units Sold", "Transactions" };
		Row header = sheet.createRow(0);
		applyHeaders(header, headers, workbook);
		int rowNum = 1;
		for (YearlySaleReport r : reports) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(r.getReportYear());
			row.createCell(1).setCellValue(r.getTotalSalesAmount().doubleValue());
			row.createCell(2).setCellValue(r.getTotalUnitsSold().doubleValue());
			row.createCell(3).setCellValue(r.getTotalTransactions());
		}
		autoSize(sheet, headers.length);
	}

	private void applyHeaders(Row headerRow, String[] headers, Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);

		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(style);
		}
	}

	private void autoSize(Sheet sheet, int columnCount) {
		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	// 🔹 Helper for import controller to get latest file path
	public String getLatestDailyReportPath() {
		return buildDailyReportPath();
	}
}

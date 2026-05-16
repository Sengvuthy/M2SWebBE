package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import SuperiorPro.SuperiorPOS.DTO.*;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleReportExportService;
import SuperiorPro.SuperiorPOS.service.SaleReportService;
import SuperiorPro.SuperiorPOS.service.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final SaleReportService saleReportService;
    private final ExcelSaleExportService excelSaleExportService;
    private final ExcelSaleImportService excelSaleImportService;
    private final ExcelSaleReportExportService excelSaleReportExportService;
    
    /** Create a new sale (invoice with items) */
    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<ApiResponse> createSale(@RequestBody SaleDTO saleDTO) {
        String invoice = saleService.sell(saleDTO);
        LocalDate date = saleDTO.getSaleDate() != null ? saleDTO.getSaleDate() : LocalDate.now();

        saleReportService.generateDailyReport(date);

        try {
            excelSaleReportExportService.exportSaleReportsToExcel();
        } catch (IOException e) {
            log.error("❌ Failed to export Excel after sale", e);
        }

        return ResponseEntity.ok(new ApiResponse("✅ Sale created successfully", invoice));
    }

    /** Update an invoice (replace items) */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateSale(@RequestBody SaleDTO saleDTO) {
        if (saleDTO.getInvoice() == null || saleDTO.getInvoice().isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse("❌ Invoice is required for update", null));
        }
        if (saleDTO.getItems() == null || saleDTO.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse("❌ At least one item is required for update", saleDTO.getInvoice()));
        }
        if (saleDTO.getCustomerName() == null || saleDTO.getCustomerName().isBlank()) {
            saleDTO.setCustomerName("General");
        }

        saleService.updateSale(saleDTO);
        return ResponseEntity.ok(new ApiResponse("✏️ Sale updated and stock adjusted", saleDTO.getInvoice()));
    }

    /** Cancel an entire invoice (restore stock) */
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse> cancelSale(@RequestBody CancelSaleRequest request) {
        saleService.cancelSaleByInvoice(request.getInvoice());
        return ResponseEntity.ok(new ApiResponse("🗑️ Sale cancelled and stock restored", request.getInvoice()));
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_CUSTOMER')")
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceSummaryDTO>> getInvoices() {
        return ResponseEntity.ok(saleService.getInvoiceSummaries());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_CUSTOMER')")
    @GetMapping("/search")
    public ResponseEntity<PageDTO<InvoiceSummaryDTO>> searchInvoices(@RequestParam Map<String, String> params) {
        Page<InvoiceSummaryDTO> invoices = saleService.getInvoices(params);
        return ResponseEntity.ok(new PageDTO<>(invoices));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN','ROLE_CUSTOMER')")
    @GetMapping("/search-by-keyword")
    public ResponseEntity<List<SaleDTO>> searchSalesByKeyword(@RequestParam String keyword) {
        return ResponseEntity.ok(saleService.searchSalesByInvoiceKeyword(keyword));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getSalesList(@RequestParam(defaultValue = "1") int _page,
                                          @RequestParam(defaultValue = "5") int _limit,
                                          @RequestParam(defaultValue = "invoice") String _sortBy,
                                          @RequestParam(defaultValue = "desc") String _sortDir,
                                          @RequestParam(required = false) String invoice) {
        return ResponseEntity.ok(saleService.getPaginatedSales(_page, _limit, _sortBy, _sortDir, invoice));
    }

    @GetMapping("/report/daily")
    public ResponseEntity<SaleReportDTO> getDailyReport(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(saleReportService.generateDailySummary(date));
    }

    @GetMapping("/report/monthly")
    public ResponseEntity<String> generateMonthlyReport(@RequestParam int year, @RequestParam int month) {
        saleReportService.generateMonthlyReport(year, month);
        return ResponseEntity.ok("✅ Monthly report generated for " + month + "/" + year);
    }

    @GetMapping("/report/yearly")
    public ResponseEntity<String> generateYearlyReport(@RequestParam int year) {
        saleReportService.generateYearlyReport(year);
        return ResponseEntity.ok("✅ Yearly report generated for " + year);
    }

    @GetMapping("/last-price")
    public ResponseEntity<BigDecimal> getLastPrice(@RequestParam Long customerId,
                                                   @RequestParam String productName) {
        BigDecimal price = saleService.getLastPriceForCustomerProduct(customerId, productName);
        return ResponseEntity.ok(price != null ? price : BigDecimal.ZERO);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/date/{date}")
    public ResponseEntity<List<SaleDTO>> getSalesByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(saleService.getSalesByDate(date));
    }

//    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN', 'ROLE_CUSTOMER')")
    @PreAuthorize("permitAll()")
    @GetMapping("/invoice/{invoice}")
    public ResponseEntity<SaleDTO> getSalesByInvoice(@PathVariable String invoice) {
        return ResponseEntity.ok(saleService.getSalesByInvoice(invoice));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/range")
    public ResponseEntity<List<SaleDTO>> getSalesByDateRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(saleService.getSalesByDateRange(start, end));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<String> importSalesFromExcel() {
        String filePath = ExcelPathResolver.resolveFixedPath("Sales");
        try {
            ImportSummary summary = excelSaleImportService.importSalesFromExcel(filePath);
            StringBuilder response = new StringBuilder()
                    .append("📥 Imported sales from: ").append(filePath).append("\n")
                    .append("⏭️ Duplicates skipped: ").append(summary.duplicates()).append("\n")
                    .append("✅ Created: ").append(summary.created()).append("\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<String> exportSaleReportsToExcel() {
        try {
            String filePath = excelSaleReportExportService.exportSaleReportsToExcel();
            return ResponseEntity.ok("✅ Exported sale reports to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/report/export/daily")
    public ResponseEntity<String> exportDailyReportsToExcel() {
        try {
            String filePath = excelSaleReportExportService.exportSaleReportsToExcel();
            return ResponseEntity.ok("✅ Exported daily reports to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }
    
    private record ApiResponse(String message, String invoice) {}
}

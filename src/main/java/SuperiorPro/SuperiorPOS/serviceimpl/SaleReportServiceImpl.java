package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.MonthlyReportDTO;
import SuperiorPro.SuperiorPOS.DTO.SaleReportDTO;
import SuperiorPro.SuperiorPOS.DTO.YearlyReportDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleReportExportService;
import SuperiorPro.SuperiorPOS.entity.MonthlySaleReport;
import SuperiorPro.SuperiorPOS.entity.Sale;
import SuperiorPro.SuperiorPOS.entity.SaleReport;
import SuperiorPro.SuperiorPOS.entity.YearlySaleReport;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.repository.MonthlySaleReportRepository;
import SuperiorPro.SuperiorPOS.repository.SaleReportRepository;
import SuperiorPro.SuperiorPOS.repository.SaleRepository;
import SuperiorPro.SuperiorPOS.repository.YearlySaleReportRepository;
import SuperiorPro.SuperiorPOS.service.SaleReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleReportServiceImpl implements SaleReportService {

    private final SaleRepository saleRepository;
    private final SaleReportRepository saleReportRepository;
    private final ExcelSaleReportExportService excelExportService;
    private final MonthlySaleReportRepository monthlyReportRepo;
    private final YearlySaleReportRepository yearlyReportRepo;

    @Override
    @Transactional
    public void generateDailyReport(LocalDate date) {
        SaleReportDTO dto = generateDailySummary(date);

        Optional<SaleReport> existing = saleReportRepository.findByReportDate(date);
        SaleReport report = existing.orElse(new SaleReport());

        report.setReportDate(dto.getReportDate());
        report.setTotalSalesAmount(dto.getTotalSalesAmount());
        report.setTotalUnitsSold(dto.getTotalUnitsSold());
        report.setTotalTransactions(dto.getTotalTransactions());

        saleReportRepository.save(report);
        log.info("✅ Daily report persisted for {}", date);

        try {
            generateMonthlyReport(date.getYear(), date.getMonthValue());
        } catch (API_Exception e) {
            log.warn("⏭️ Monthly generation skipped: {}", e.getMessage());
        }

        try {
            generateYearlyReport(date.getYear());
        } catch (API_Exception e) {
            log.warn("⏭️ Yearly generation skipped: {}", e.getMessage());
        }

        try {
            String filePath = excelExportService.exportMonthlyReportsToExcel();
            log.info("📤 Monthly Excel exported to {}", filePath);
        } catch (IOException e) {
            log.error("❌ Failed to export monthly Excel", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SaleReportDTO generateDailySummary(LocalDate date) {
        List<Sale> sales = saleRepository.findBySaleDate(date);
        if (sales.isEmpty()) {
            throw new API_Exception(HttpStatus.NOT_FOUND, "No sales found for date: " + date);
        }

        BigDecimal totalAmount = sales.stream()
                .map(Sale::getSoldAmount)
                .filter(u -> u != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnits = sales.stream()
                .map(Sale::getNumberOfUnit)
                .filter(u -> u != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int transactionCount = sales.size();

        return new SaleReportDTO(date, totalAmount, totalUnits, transactionCount);
    }

    @Override
    @Transactional
    public MonthlyReportDTO generateMonthlyReport(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<SaleReport> dailyReports = saleReportRepository.findByReportDateBetween(start, end);
        if (dailyReports.isEmpty()) {
            throw new API_Exception(HttpStatus.NOT_FOUND, "No daily reports found for " + year + "-" + month);
        }

        BigDecimal totalAmount = dailyReports.stream()
                .map(SaleReport::getTotalSalesAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnits = dailyReports.stream()
                .map(SaleReport::getTotalUnitsSold)
                .filter(u -> u != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int transactionCount = dailyReports.stream()
                .mapToInt(SaleReport::getTotalTransactions)
                .sum();

        Optional<MonthlySaleReport> existing = monthlyReportRepo.findByReportYearAndReportMonth(year, month);
        MonthlySaleReport report = existing.orElse(new MonthlySaleReport());

        report.setReportMonth(month);
        report.setReportYear(year);
        report.setGeneratedAt(LocalDate.now());
        report.setTotalSalesAmount(totalAmount);
        report.setTotalUnitsSold(totalUnits);
        report.setTotalTransactions(transactionCount);

        monthlyReportRepo.save(report);
        log.info("✅ Monthly report persisted for {}/{}", month, year);

        List<SaleReportDTO> dailySummaries = dailyReports.stream()
                .map(r -> new SaleReportDTO(r.getReportDate(),
                        r.getTotalSalesAmount(),
                        r.getTotalUnitsSold(),
                        r.getTotalTransactions()))
                .toList();

        return new MonthlyReportDTO(year, month, totalAmount, totalUnits, transactionCount,
                report.getGeneratedAt(), dailySummaries);
    }

    @Override
    @Transactional
    public void generateYearlyReport(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);
        if (sales.isEmpty()) {
            throw new API_Exception(HttpStatus.NOT_FOUND, "No sales found for year: " + year);
        }

        BigDecimal totalAmount = sales.stream()
                .map(Sale::getSoldAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnits = sales.stream()
                .map(Sale::getNumberOfUnit)
                .filter(u -> u != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int transactionCount = sales.size();

        Optional<YearlySaleReport> existing = yearlyReportRepo.findByReportYear(year);
        YearlySaleReport report = existing.orElse(new YearlySaleReport());

        report.setReportYear(year);
        report.setTotalSalesAmount(totalAmount);
        report.setTotalUnitsSold(totalUnits);
        report.setTotalTransactions(transactionCount);
        report.setGeneratedAt(LocalDate.now());

        yearlyReportRepo.save(report);
        log.info("✅ Yearly report persisted for {}", year);
    }

    @Override
    @Transactional
    public void generateDailyReportsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Start date must be before or equal to end date");
        }

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            try {
                generateDailyReport(current);
            } catch (API_Exception e) {
                log.warn("⏭️ Skipping date {}: {}", current, e.getMessage());
            }
            current = current.plusDays(1);
        }
    }

    @Override
    @Transactional
    public void generateMonthlyReportsBetween(int startYear, int startMonth, int endYear, int endMonth) {
        LocalDate start = LocalDate.of(startYear, startMonth, 1);
        LocalDate end = LocalDate.of(endYear, endMonth, 1);

        if (start.isAfter(end)) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Start month must be before or equal to end month");
        }

        LocalDate current = start;
        while (!current.isAfter(end)) {
            try {
                generateMonthlyReport(current.getYear(), current.getMonthValue());
            } catch (API_Exception e) {
                log.warn("⏭️ Skipping {}-{}: {}", current.getYear(), current.getMonthValue(), e.getMessage());
            }
            current = current.plusMonths(1);
        }
    }

    @Transactional(readOnly = true)
    public YearlyReportDTO getYearlyReportDTO(int year) {
        YearlySaleReport report = yearlyReportRepo.findByReportYear(year)
                .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND, "No yearly report found for " + year));

        List<MonthlySaleReport> monthlyReports = monthlyReportRepo.findByReportYearOrderByReportMonthAsc(year);
        List<MonthlyReportDTO> monthlySummaries = monthlyReports.stream()
                .map(m -> new MonthlyReportDTO(
                        m.getReportYear(),
                        m.getReportMonth(),
                        m.getTotalSalesAmount(),
                        m.getTotalUnitsSold(),
                        m.getTotalTransactions(),
                        m.getGeneratedAt(),
                        List.of()))
                .toList();

        return new YearlyReportDTO(
                report.getReportYear(),
                report.getTotalSalesAmount(),
                report.getTotalUnitsSold(),
                report.getTotalTransactions(),
                report.getGeneratedAt(),
                monthlySummaries
        );
    }

    @Override
    @Transactional
    public void generateYearlyReportsBetween(int startYear, int endYear) {
        if (startYear > endYear) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Start year must be before or equal to end year");
        }

        for (int year = startYear; year <= endYear; year++) {
            try {
                generateYearlyReport(year);
            } catch (API_Exception e) {
                log.warn("⏭️ Skipping year {}: {}", year, e.getMessage());
            }
        }
    }
}

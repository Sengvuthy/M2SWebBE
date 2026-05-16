package SuperiorPro.SuperiorPOS.service;

import java.time.LocalDate;

import SuperiorPro.SuperiorPOS.DTO.MonthlyReportDTO;
import SuperiorPro.SuperiorPOS.DTO.SaleReportDTO;
import SuperiorPro.SuperiorPOS.DTO.YearlyReportDTO;

public interface SaleReportService {
    void generateDailyReport(LocalDate date);

    SaleReportDTO generateDailySummary(LocalDate date);

    MonthlyReportDTO generateMonthlyReport(int year, int month);

    void generateYearlyReport(int year);

    YearlyReportDTO getYearlyReportDTO(int year); // ✅ Add this line

    void generateDailyReportsBetween(LocalDate startDate, LocalDate endDate);

    void generateMonthlyReportsBetween(int startYear, int startMonth, int endYear, int endMonth);

    void generateYearlyReportsBetween(int startYear, int endYear);
}

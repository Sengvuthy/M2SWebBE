package SuperiorPro.SuperiorPOS.mapper;

import SuperiorPro.SuperiorPOS.DTO.SaleReportDTO;
import SuperiorPro.SuperiorPOS.entity.SaleReport;

public class SaleReportMapper {

    public static SaleReportDTO toDTO(SaleReport report) {
        SaleReportDTO dto = new SaleReportDTO();
        dto.setReportDate(report.getReportDate());
        dto.setTotalSalesAmount(report.getTotalSalesAmount());
        dto.setTotalUnitsSold(report.getTotalUnitsSold());
        dto.setTotalTransactions(report.getTotalTransactions());
        return dto;
    }

    public static SaleReport toEntity(SaleReportDTO dto) {
        SaleReport report = new SaleReport();
        report.setReportDate(dto.getReportDate());
        report.setTotalSalesAmount(dto.getTotalSalesAmount());
        report.setTotalUnitsSold(dto.getTotalUnitsSold());
        report.setTotalTransactions(dto.getTotalTransactions());
        return report;
    }
}

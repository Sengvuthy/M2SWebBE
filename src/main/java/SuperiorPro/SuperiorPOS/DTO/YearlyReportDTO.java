package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class YearlyReportDTO {
	
    private Integer reportYear;
    private BigDecimal totalSalesAmount;
    private BigDecimal totalUnitsSold;
    private Integer totalTransactions;
    private LocalDate generatedAt;
    private List<MonthlyReportDTO> monthlySummaries;
}

package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportDTO {
	
    private int reportYear;
    private int reportMonth;
    private BigDecimal totalSalesAmount;
    private BigDecimal totalUnitsSold;
    private int totalTransactions;
    private LocalDate generatedAt;

    // 🔹 Include daily breakdown
    private List<SaleReportDTO> dailySummaries;
}

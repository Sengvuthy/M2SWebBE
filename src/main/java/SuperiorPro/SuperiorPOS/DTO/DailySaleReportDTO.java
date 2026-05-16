package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class DailySaleReportDTO {
	
    private LocalDate reportDate;
    private BigDecimal totalSalesAmount;
    private BigDecimal totalUnitsSold;
    private Integer totalTransactions;
}

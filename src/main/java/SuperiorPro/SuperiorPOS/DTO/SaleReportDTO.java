package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleReportDTO {
	
    private LocalDate reportDate;
    private BigDecimal totalSalesAmount;
    private BigDecimal totalUnitsSold;
    private Integer totalTransactions;
}

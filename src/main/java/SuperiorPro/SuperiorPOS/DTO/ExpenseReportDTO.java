package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class ExpenseReportDTO {
	
    private String expenseId;
    private String supplierName;
    private String payerName;
    private String source; // CREATION | IMPORT | OTHER
    private LocalDate expenseDate;
    private LocalTime expenseTime;
    private String productName;
    private BigDecimal expenseUnit;
    private BigDecimal expensePrice;
    private BigDecimal expenseAmount; // expensePrice × expenseUnit
    
    private List<ExpenseReportItem> items;
}

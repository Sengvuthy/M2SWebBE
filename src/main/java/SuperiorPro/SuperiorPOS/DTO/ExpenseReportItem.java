package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ExpenseReportItem {

    private String productName;
    private String source; // CREATION | IMPORT | OTHER
    private BigDecimal expenseUnit;
    private BigDecimal expensePrice;
}

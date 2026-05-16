package SuperiorPro.SuperiorPOS.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseIdSummaryDTO {
	
    private String expenseId;
    private String supplierName;
    private String payerName;
    private LocalDate expenseDate;
    private LocalTime expenseTime;
    private int itemCount;
    private double totalAmount;
    private String source;
}

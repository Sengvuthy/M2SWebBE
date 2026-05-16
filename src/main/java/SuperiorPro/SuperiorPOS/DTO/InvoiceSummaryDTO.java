package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSummaryDTO {
	
    private String invoice;
    private String customerName;
    private String sellerName;
    private LocalDate saleDate;
    private LocalTime saleTime;
    private int itemCount;
    private BigDecimal totalAmount;
}

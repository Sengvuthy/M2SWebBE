package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InvoiceSummaryDTO {
    private String invoice;
    private String customerName;
    private LocalDate saleDate;
    private LocalTime saleTime;
    private LocalDateTime dateTime;
    private Long itemCount;
    private BigDecimal totalAmount;

    public InvoiceSummaryDTO(String invoice,
                             String customerName,
                             LocalDate saleDate,
                             LocalTime saleTime,
                             LocalDateTime dateTime,
                             Long itemCount,
                             BigDecimal totalAmount) {
        this.invoice = invoice;
        this.customerName = customerName;
        this.saleDate = saleDate;
        this.saleTime = saleTime;
        this.dateTime = dateTime;
        this.itemCount = itemCount;
        this.totalAmount = totalAmount;
    }
}

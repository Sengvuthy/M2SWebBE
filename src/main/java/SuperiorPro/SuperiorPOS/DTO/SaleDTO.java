package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class SaleDTO {

    private String invoice;
    private Long customerId;
    private String customerName;
    private LocalDate saleDate;
    private LocalTime saleTime;
    private LocalDateTime dateTime;

    private String barcode;
    private String productName;
    private String khmerName;
    private BigDecimal numberOfUnit;
    private BigDecimal unitPrice;
    private BigDecimal soldAmount;

    private List<SaleItem> items;
}

package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    // optional single-line fields for one-off sales
    private String barcode;
    private String productName;
    private String khmerName;
    private BigDecimal numberOfUnit;
    private BigDecimal unitPrice;
    private BigDecimal soldAmount;

    // 🔹 list of product lines
    private List<SaleItem> items;
}

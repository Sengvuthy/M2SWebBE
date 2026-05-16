package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductSalesSummaryDTO {
    private String barcode;
    private String productName;
    private BigDecimal totalUnitsSold;
    private BigDecimal totalRevenue;
}

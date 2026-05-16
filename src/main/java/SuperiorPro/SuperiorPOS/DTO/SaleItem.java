package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SaleItem {
	
    private String barcode;
    private String productName;
    private String khmerName;
    private BigDecimal numberOfUnit;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal soldAmount;
}

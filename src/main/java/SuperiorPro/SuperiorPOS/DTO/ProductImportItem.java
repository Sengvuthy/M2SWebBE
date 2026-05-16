package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductImportItem {
	
	private String barcode;
	private String productName;
	private String khmerName;
	private BigDecimal importUnit;
	private BigDecimal buyPrice;
	private BigDecimal salePrice;
}

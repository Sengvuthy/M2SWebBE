package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductSaleDTO {

	private String barcode;
	private BigDecimal numberOfUnit;
}

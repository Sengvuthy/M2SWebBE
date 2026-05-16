package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class ProductImportDTO {
	
	private String importId;
	private String supplierName;
	private String importerName;
	private LocalDate importDate;
	private LocalTime importTime;
	
	private String barcode;
    private String productName;
    private String khmerName;
    private BigDecimal importUnit;
    private BigDecimal buyAmount; // sum of buyPrice × importUnit
    
	private List<ProductImportItem> items;
}

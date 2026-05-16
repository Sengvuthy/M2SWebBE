package SuperiorPro.SuperiorPOS.DTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductDTO {

    @NotBlank(message = "Barcode is required")
    private String barcode;

    @NotBlank(message = "Name is required")
    private String name;
    private String khmerName;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private BigDecimal availableUnit;
    private BigDecimal buyPrice;
    private BigDecimal salePrice;
    private String imagePath;

    // ✅ optional, filled server-side for display
    private String categoryName;
    private String supplierName;
}

package SuperiorPro.SuperiorPOS.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_barcode", nullable = false, unique = true)
    private String barcode;

    @Column(name = "product_name", nullable = false)
    private String name;
    
    @Column(name = "khmer_name", nullable = false)
    private String khmerName;

    @Column(name = "available_unit")
    private BigDecimal availableUnit;

    @DecimalMin(value = "0.00000", message = "Buy price at least equal to 0")
    @Column(name = "buy_price")
    private BigDecimal buyPrice;

    @DecimalMin(value = "0.00001", message = "Sale price must be greater than 0")
    @Column(name = "sale_price")
    private BigDecimal salePrice;

    // ✅ normalized foreign keys
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "supplier_id")
    private Long supplierId;

    // ✅ optional convenience fields (not required, can be filled by service)
    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "image_path")
    private String imagePath;
}

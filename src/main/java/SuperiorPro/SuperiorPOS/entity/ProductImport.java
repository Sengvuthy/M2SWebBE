package SuperiorPro.SuperiorPOS.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "product_imports")
public class ProductImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "import_id", nullable = false)
    private String importId;

    @Column(name = "barcode", nullable = false)
    private String barcode;

    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "khmer_name", nullable = false)
    private String khmerName;

    @Column(name = "import_unit", nullable = false)
    private BigDecimal importUnit;

    @Column(name = "buy_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal buyPrice;   // ✅ new field: unit price at import time

    @Column(name = "buy_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal buyAmount;  // total cost = buyPrice × importUnit

    @Column(name = "sale_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal salePrice;

    @Column(name = "import_date", nullable = false)
    private LocalDate importDate;

    @Column(name = "import_time", nullable = false)
    private LocalTime importTime;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "importer_name", nullable = false)
    private String importerName;
}

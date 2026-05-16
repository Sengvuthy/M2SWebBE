package SuperiorPro.SuperiorPOS.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sale_report_yearly")
public class YearlySaleReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_year", nullable = false, unique = true)
    private Integer reportYear;

    @Column(name = "total_sales_amount", nullable = false)
    private BigDecimal totalSalesAmount;

    @Column(name = "total_units_sold", nullable = false)
    private BigDecimal totalUnitsSold;

    @Column(name = "total_transactions", nullable = false)
    private Integer totalTransactions;

    @Column(name = "generated_at", nullable = false)
    private java.time.LocalDate generatedAt;
}

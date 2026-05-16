package SuperiorPro.SuperiorPOS.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "daily_sale_reports")
public class DailySaleReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    @Column(name = "total_sales_amount", nullable = false)
    private BigDecimal totalSalesAmount;

    @Column(name = "total_units_sold", nullable = false)
    private BigDecimal totalUnitsSold;

    @Column(name = "total_transactions", nullable = false)
    private Integer totalTransactions;
}

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
@Table(name = "sale_report_daily")
public class SaleReport {
	// This class is used with SaleDetail class to create Daily, Monthly, Yearly Report

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    @Column(name = "total_sales_amount", nullable = false)
    private BigDecimal totalSalesAmount;
    
    @Column(name = "total_transactions", nullable = false)
    private Integer totalTransactions;

    @Column(name = "total_units_sold", nullable = false)
    private BigDecimal totalUnitsSold;
}

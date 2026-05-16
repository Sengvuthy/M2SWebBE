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
@Table(name = "expense_reports")
public class ExpenseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expense_id", nullable = false)
    private String expenseId;

    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "expense_unit", nullable = false)
    private BigDecimal expenseUnit;

    @Column(name = "expense_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal expensePrice;   // unit price

    @Column(name = "expense_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal expenseAmount;  // expensePrice × expenseUnit
    
    @Column(name = "source", nullable = false)
    private String source; // "Creation Expense" | "Import Expense" | "Other Expense"

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "expense_time", nullable = false)
    private LocalTime expenseTime;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "payer_name", nullable = false)
    private String payerName;
}

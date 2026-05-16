package SuperiorPro.SuperiorPOS.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.entity.ExpenseReport;

@Repository
public interface ExpenseReportRepository extends JpaRepository<ExpenseReport, Long> {

    /** Get the most recent expense record (highest id) */
    ExpenseReport findTopByOrderByIdDesc();

    /** Find all expenses by exact expenseId */
    List<ExpenseReport> findByExpenseId(String expenseId);

    /** Search expenses by partial expenseId (case-insensitive) */
    List<ExpenseReport> findByExpenseIdContainingIgnoreCase(String keyword);

    /** Find expenses by exact date */
    List<ExpenseReport> findByExpenseDate(LocalDate date);

    /** Find expenses between two dates */
    List<ExpenseReport> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    /** Find expenses by product name (case-insensitive) */
    List<ExpenseReport> findByProductNameIgnoreCase(String productName);
    
    /** Find created product to updates by product name (case-insensitive) */
    List<ExpenseReport> findByProductNameAndSource(String productName, String source);

    /** Get distinct expenseIds with optional filter */
    @Query("SELECT DISTINCT p.expenseId FROM ExpenseReport p WHERE (:expenseId IS NULL OR p.expenseId LIKE %:expenseId%)")
    Page<String> findDistinctExpenseIds(@Param("expenseId") String expenseId, Pageable pageable);

    /** Paginated search by expenseId keyword */
    Page<ExpenseReport> findDistinctByExpenseIdContainingIgnoreCase(String expenseId, Pageable pageable);
}

package SuperiorPro.SuperiorPOS.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import SuperiorPro.SuperiorPOS.DTO.ExpenseIdSummaryDTO;
import SuperiorPro.SuperiorPOS.DTO.ExpenseReportDTO;

public interface ExpenseReportService {
	
	String generateNextExpenseId();

    /** Expense new products (creates a new expenseId batch) */
    ExpenseReportDTO createExpense(ExpenseReportDTO expenseReportDTO);

    /** Cancel an entire expense batch by expenseId (restores stock) */
    void cancelExpenseReportByExpenseId(String expenseId);

    /** Update an existing expense batch (replace items, adjust stock) */
    void updateExpenseReport(ExpenseReportDTO expenseReportDTO);

    /** Get summaries of all expenseIds (grouped view with totals) */
    List<ExpenseIdSummaryDTO> getExpenseIdSummaries();

    /** Get a single expense batch by expenseId (grouped DTO with items and totals) */
    ExpenseReportDTO getExpenseReportsByExpenseId(String expenseId);

    /** Get all expenses for a specific date */
    List<ExpenseReportDTO> getExpenseReportsByDate(LocalDate date);

    /** Get all expenses within a date range */
    List<ExpenseReportDTO> getExpenseReportsByDateRange(LocalDate start, LocalDate end);

    /** Search expenses by keyword in expenseId */
    List<ExpenseReportDTO> searchExpenseReportsByExpenseIdKeyword(String keyword);

    /** Paginated list of expenses with sorting and optional filter */
    Map<String, Object> getPaginatedExpenseReports(int page, int limit, String sortBy, String sortDir, String expenseId);

    /** Paginated summaries of distinct expenseIds */
    Page<ExpenseIdSummaryDTO> getExpenseIds(Map<String,String> params);
}

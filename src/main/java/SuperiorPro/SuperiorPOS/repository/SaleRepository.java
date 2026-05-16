package SuperiorPro.SuperiorPOS.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import SuperiorPro.SuperiorPOS.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {

	// 🔹 Fetch the latest sale by ID
	Sale findTopByOrderByIdDesc();

	// 🔹 Fetch the latest sale by invoice (lexicographically highest)
	Sale findTopByOrderByInvoiceDesc();

	// 🔹 Exact match by invoice
	List<Sale> findByInvoice(String invoice);

	// 🔹 Partial search by invoice (case-insensitive)
	List<Sale> findByInvoiceContainingIgnoreCase(String keyword);

	// 🔹 Distinct invoices with pagination (case-insensitive)
	Page<Sale> findDistinctByInvoiceContainingIgnoreCase(String invoice, Pageable pageable);

	// 🔹 To show last price of product which customer used to buy
	Sale findTopByCustomerIdAndProductNameOrderBySaleDateDescSaleTimeDesc(Long customerId, String productName);

	// 🔹 Fetch sales by exact date
	List<Sale> findBySaleDate(LocalDate date);

	// 🔹 Fetch sales between two dates (inclusive)
	List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);

	// 🔹 Native query for distinct invoices (with optional filter)
	@Query(value = """
			SELECT DISTINCT s.invoice
			FROM sales s
			WHERE (:invoice IS NULL OR s.invoice ILIKE '%' || CAST(:invoice AS varchar) || '%')
			""", nativeQuery = true)
	Page<String> findDistinctInvoices(@Param("invoice") String invoice, Pageable pageable);

	// 🔹 Check if a sale exists by invoice + barcode
	boolean existsByInvoiceAndBarcode(String invoice, String barcode);
}

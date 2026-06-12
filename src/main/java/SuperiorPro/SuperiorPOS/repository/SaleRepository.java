package SuperiorPro.SuperiorPOS.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import SuperiorPro.SuperiorPOS.DTO.InvoiceSummaryDTO;
import SuperiorPro.SuperiorPOS.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {

	Sale findTopByOrderByIdDesc();
	Sale findTopByOrderByInvoiceDesc();
	List<Sale> findByInvoice(String invoice);
	List<Sale> findByInvoiceContainingIgnoreCase(String keyword);
	Page<Sale> findDistinctByInvoiceContainingIgnoreCase(String invoice, Pageable pageable);
	Sale findTopByCustomerIdAndProductNameOrderBySaleDateDescSaleTimeDesc(Long customerId, String productName);
	List<Sale> findBySaleDate(LocalDate date);
	List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);
	
	// ✅ Invoice number generator
    @Query(value = "SELECT MAX(CAST(SUBSTRING(invoice, 5) AS INTEGER)) FROM sales", nativeQuery = true)
    Integer findMaxInvoiceNumber();

    @Query("""
            SELECT new SuperiorPro.SuperiorPOS.DTO.InvoiceSummaryDTO(
                s.invoice,
                MAX(s.customerName),
                MAX(s.saleDate),
                MAX(s.saleTime),
                MAX(s.dateTime),
                COUNT(s),
                COALESCE(SUM(s.soldAmount), 0)
            )
            FROM Sale s
            WHERE (:invoice IS NULL OR s.invoice LIKE %:invoice%)
            GROUP BY s.invoice
            ORDER BY MAX(s.dateTime) DESC
            """)
    Page<InvoiceSummaryDTO> findInvoiceSummaries(@Param("invoice") String invoice, Pageable pageable);

	boolean existsByInvoiceAndBarcode(String invoice, String barcode);
}

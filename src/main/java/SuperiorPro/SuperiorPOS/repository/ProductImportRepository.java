package SuperiorPro.SuperiorPOS.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.entity.ProductImport;

@Repository
public interface ProductImportRepository extends JpaRepository<ProductImport, Long> {

    /** Get the most recent import record (highest id) */
    ProductImport findTopByOrderByIdDesc();

    /** Find all imports by exact importId */
    List<ProductImport> findByImportId(String importId);

    /** Search imports by partial importId (case-insensitive) */
    List<ProductImport> findByImportIdContainingIgnoreCase(String keyword);

    /** Find imports by exact date */
    List<ProductImport> findByImportDate(LocalDate date);

    /** Find imports between two dates */
    List<ProductImport> findByImportDateBetween(LocalDate startDate, LocalDate endDate);

    /** Find imports by product barcode */
    List<ProductImport> findByBarcode(String barcode);

    /** Find imports by product name (case-insensitive) */
    List<ProductImport> findByProductNameIgnoreCase(String productName);

    /** Find a single import by barcode and date (used to detect duplicates/updates) */
    List<ProductImport> findByBarcodeAndImportDate(String barcode, LocalDate importDate);

    /** Get distinct importIds with optional filter */
    @Query("SELECT DISTINCT p.importId FROM ProductImport p WHERE (:importId IS NULL OR p.importId LIKE %:importId%)")
    Page<String> findDistinctImportIds(@Param("importId") String importId, Pageable pageable);

    /** Paginated search by importId keyword */
    Page<ProductImport> findDistinctByImportIdContainingIgnoreCase(String importId, Pageable pageable);

    /** Check if a product already exists in a given import batch */
    boolean existsByImportIdAndBarcode(String importId, String barcode);
}

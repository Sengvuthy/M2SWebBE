package SuperiorPro.SuperiorPOS.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByNameIgnoreCase(String name);

    // Keep the original English-only search
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // ✅ Add the new method for English OR Khmer search
    Page<Product> findByNameContainingIgnoreCaseOrKhmerNameContainingIgnoreCase(String name, String khmerName,
            Pageable pageable);

    // Search Name and Barcode
    @Query("SELECT p FROM Product p " + "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(p.khmerName) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByNameOrBarcode(@Param("keyword") String keyword, Pageable pageable);

    void deleteByNameIgnoreCase(String name);

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findBySupplierId(Long supplierId, Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.availableUnit = p.availableUnit + :unit WHERE p.barcode = :barcode")
    void incrementAvailableUnit(@Param("barcode") String barcode, @Param("unit") BigDecimal unit);
}

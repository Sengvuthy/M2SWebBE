package SuperiorPro.SuperiorPOS.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import SuperiorPro.SuperiorPOS.entity.SaleDetail;

public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {

    @Query("SELECT sd FROM SaleDetail sd WHERE LOWER(sd.product.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<SaleDetail> findByProductNameIgnoreCase(String name);

    List<SaleDetail> findBySale_SaleDate(java.time.LocalDate date);
}

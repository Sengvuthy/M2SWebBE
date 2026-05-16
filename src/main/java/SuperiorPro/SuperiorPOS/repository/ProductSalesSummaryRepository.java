package SuperiorPro.SuperiorPOS.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.DTO.ProductSalesSummaryDTO;
import SuperiorPro.SuperiorPOS.entity.Sale;

@Repository
public interface ProductSalesSummaryRepository extends JpaRepository<Sale, Long> {

	@Query("""
		    SELECT new SuperiorPro.SuperiorPOS.DTO.ProductSalesSummaryDTO(
		        s.barcode,
		        s.productName,
		        SUM(s.numberOfUnit),
		        SUM(s.soldAmount)
		    )
		    FROM Sale s
		    GROUP BY s.barcode, s.productName
		    ORDER BY SUM(s.numberOfUnit) DESC
		""")
		List<ProductSalesSummaryDTO> findProductSalesSummaryOrderedByUnitsDesc();

	@Query("""
		    SELECT new SuperiorPro.SuperiorPOS.DTO.ProductSalesSummaryDTO(
		        s.barcode,
		        s.productName,
		        SUM(s.numberOfUnit),
		        SUM(s.soldAmount)
		    )
		    FROM Sale s
		    GROUP BY s.barcode, s.productName
		    ORDER BY SUM(s.soldAmount) DESC
		""")
		List<ProductSalesSummaryDTO> findProductSalesSummaryOrderedByRevenueDesc();
	
	@Query("""
		    SELECT new SuperiorPro.SuperiorPOS.DTO.ProductSalesSummaryDTO(
		        s.barcode,
		        s.productName,
		        SUM(s.numberOfUnit),
		        SUM(s.soldAmount)
		    )
		    FROM Sale s
		    WHERE s.saleDate BETWEEN :startDate AND :endDate
		    GROUP BY s.barcode, s.productName
		    ORDER BY SUM(s.numberOfUnit) DESC
		""")
		List<ProductSalesSummaryDTO> findSummaryByUnitsWithinDate(LocalDate startDate, LocalDate endDate);
	
	@Query("""
		    SELECT new SuperiorPro.SuperiorPOS.DTO.ProductSalesSummaryDTO(
		        s.barcode,
		        s.productName,
		        SUM(s.numberOfUnit),
		        SUM(s.soldAmount)
		    )
		    FROM Sale s
		    WHERE s.saleDate BETWEEN :startDate AND :endDate
		    GROUP BY s.barcode, s.productName
		    ORDER BY SUM(s.soldAmount) DESC
		""")
		List<ProductSalesSummaryDTO> findSummaryByRevenueWithinDate(LocalDate startDate, LocalDate endDate);
}

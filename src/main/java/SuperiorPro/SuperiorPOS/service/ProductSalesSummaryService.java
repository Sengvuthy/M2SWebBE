package SuperiorPro.SuperiorPOS.service;

import java.time.LocalDate;
import java.util.List;

import SuperiorPro.SuperiorPOS.DTO.ProductSalesSummaryDTO;

public interface ProductSalesSummaryService {

	List<ProductSalesSummaryDTO> getProductSalesSummaryOrderedByUnits();
	List<ProductSalesSummaryDTO> getProductSalesSummaryOrderedByRevenue();
	List<ProductSalesSummaryDTO> getSummaryByUnitsWithinDate(LocalDate startDate, LocalDate endDate);
	List<ProductSalesSummaryDTO> getSummaryByRevenueWithinDate(LocalDate startDate, LocalDate endDate);
}

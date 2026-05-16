package SuperiorPro.SuperiorPOS.serviceimpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.DTO.ProductSalesSummaryDTO;
import SuperiorPro.SuperiorPOS.repository.ProductSalesSummaryRepository;
import SuperiorPro.SuperiorPOS.service.ProductSalesSummaryService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSalesSummaryServiceImpl implements ProductSalesSummaryService{

	private final ProductSalesSummaryRepository productSalesSummaryRepository;
	
	@Override
	public List<ProductSalesSummaryDTO> getProductSalesSummaryOrderedByUnits() {
	    return productSalesSummaryRepository.findProductSalesSummaryOrderedByUnitsDesc();
	}

	@Override
	public List<ProductSalesSummaryDTO> getProductSalesSummaryOrderedByRevenue() {
	    return productSalesSummaryRepository.findProductSalesSummaryOrderedByRevenueDesc();
	}
	
	@Override
	public List<ProductSalesSummaryDTO> getSummaryByUnitsWithinDate(LocalDate startDate, LocalDate endDate) {
	    return productSalesSummaryRepository.findSummaryByUnitsWithinDate(startDate, endDate);
	}
	
	@Override
	public List<ProductSalesSummaryDTO> getSummaryByRevenueWithinDate(LocalDate startDate, LocalDate endDate) {
	    return productSalesSummaryRepository.findSummaryByRevenueWithinDate(startDate, endDate);
	}
}

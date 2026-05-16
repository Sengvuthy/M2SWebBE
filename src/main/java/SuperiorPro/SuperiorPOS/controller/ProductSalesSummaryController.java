package SuperiorPro.SuperiorPOS.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.DTO.ProductSalesSummaryDTO;
import SuperiorPro.SuperiorPOS.service.ProductSalesSummaryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product_sales")
@RequiredArgsConstructor
public class ProductSalesSummaryController {

    private final ProductSalesSummaryService productSalesSummaryService;

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN', 'ROLE_SELLER', 'ROLE_DELIVERY')")
    @GetMapping("/summary/by-units")
    public ResponseEntity<List<ProductSalesSummaryDTO>> getSalesSummaryByUnits() {
        return ResponseEntity.ok(productSalesSummaryService.getProductSalesSummaryOrderedByUnits());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN', 'ROLE_SELLER', 'ROLE_DELIVERY')")
    @GetMapping("/summary/by-revenue")
    public ResponseEntity<List<ProductSalesSummaryDTO>> getSalesSummaryByRevenue() {
        return ResponseEntity.ok(productSalesSummaryService.getProductSalesSummaryOrderedByRevenue());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN', 'ROLE_SELLER', 'ROLE_DELIVERY')")
    @GetMapping("/summary/by-units/date-range")
    public ResponseEntity<List<ProductSalesSummaryDTO>> getSummaryByUnitsWithinDate(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(List.of());
        }
        return ResponseEntity.ok(
            productSalesSummaryService.getSummaryByUnitsWithinDate(startDate, endDate)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN', 'ROLE_SELLER', 'ROLE_DELIVERY')")
    @GetMapping("/summary/by-revenue/date-range")
    public ResponseEntity<List<ProductSalesSummaryDTO>> getSummaryByRevenueWithinDate(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        return ResponseEntity.ok(
            productSalesSummaryService.getSummaryByRevenueWithinDate(startDate, endDate)
        );
    }
}

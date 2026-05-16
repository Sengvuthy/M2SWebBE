package SuperiorPro.SuperiorPOS.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import SuperiorPro.SuperiorPOS.DTO.InvoiceSummaryDTO;
import SuperiorPro.SuperiorPOS.DTO.SaleDTO;

public interface SaleService {

    // Create a new sale
    String sell(SaleDTO saleDTO);

    // Update an existing sale
    void updateSale(SaleDTO saleDTO);

    // Cancel a sale by invoice
    void cancelSaleByInvoice(String invoice);

    // Summaries for reporting
    List<InvoiceSummaryDTO> getInvoiceSummaries();

    // Last price a customer paid for a product
    BigDecimal getLastPriceForCustomerProduct(Long customerId, String productName);

    // Return one grouped DTO per invoice
    SaleDTO getSalesByInvoice(String invoice);

    // Fetch sales by date or range
    List<SaleDTO> getSalesByDate(LocalDate date);
    List<SaleDTO> getSalesByDateRange(LocalDate start, LocalDate end);

    // Search by invoice keyword
    List<SaleDTO> searchSalesByInvoiceKeyword(String keyword);

    // Paginated sales list
    Map<String, Object> getPaginatedSales(int page, int limit, String sortBy, String sortDir, String invoice);

    // Paginated invoice summaries
    Page<InvoiceSummaryDTO> getInvoices(Map<String,String> params);
}

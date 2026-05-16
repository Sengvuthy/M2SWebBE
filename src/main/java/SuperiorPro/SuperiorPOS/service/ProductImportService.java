package SuperiorPro.SuperiorPOS.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import SuperiorPro.SuperiorPOS.DTO.ImportIdSummaryDTO;
import SuperiorPro.SuperiorPOS.DTO.ProductImportDTO;

public interface ProductImportService {

    /** Import new products (creates a new importId batch) */
    ProductImportDTO importProduct(ProductImportDTO productImportDTO);

    /** Cancel an entire import batch by importId (restores stock) */
    void cancelProductImportByImportId(String importId);

    /** Update an existing import batch (replace items, adjust stock) */
    void updateProductImport(ProductImportDTO productImportDTO);

    /** Get summaries of all importIds (grouped view with totals) */
    List<ImportIdSummaryDTO> getImportIdSummaries();

    /** Get a single import batch by importId (grouped DTO with items and totals) */
    ProductImportDTO getProductImportsByImportId(String importId);

    /** Get all imports for a specific date */
    List<ProductImportDTO> getProductImportsByDate(LocalDate date);

    /** Get all imports within a date range */
    List<ProductImportDTO> getProductImportsByDateRange(LocalDate start, LocalDate end);

    /** Search imports by keyword in importId */
    List<ProductImportDTO> searchProductImportsByImportIdKeyword(String keyword);

    /** Paginated list of imports with sorting and optional filter */
    Map<String, Object> getPaginatedProductImports(int page, int limit, String sortBy, String sortDir, String importId);

    /** Paginated summaries of distinct importIds */
    Page<ImportIdSummaryDTO> getImportIds(Map<String,String> params);
}

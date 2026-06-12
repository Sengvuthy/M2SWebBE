package SuperiorPro.SuperiorPOS.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import SuperiorPro.SuperiorPOS.DTO.ProductDTO;
import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.entity.ProductImport;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    Product save(ProductDTO dto);
    Product save(Product product);

    Product getById(Long id);
    Product getByName(String name);
    Product getByBarcode(String barcode);

    Product updateByBarcode(String barcode, ProductDTO dto);
    Product updateProductImage(String barcode, MultipartFile file);

    Page<Product> searchByNameOrBarcode(String keyword, Pageable pageable);
    Page<Product> getProducts(String name, Pageable pageable);

    void deleteByName(String name);
    void deleteByBarcode(String barcode);

    void validateStock(Long productId, BigDecimal numberOfUnit);

    List<ProductImport> getImportHistoryByBarcode(String barcode);
    List<ProductImport> getImportHistoryByName(String name);

    // ✅ New methods for normalized model
    Page<Product> getProductsByCategory(Long categoryId, Pageable pageable);
    Page<Product> getProductsBySupplier(Long supplierId, Pageable pageable);
}

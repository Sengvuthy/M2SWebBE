package SuperiorPro.SuperiorPOS;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.exception.ResourceNotFoundException;
import SuperiorPro.SuperiorPOS.service.ProductService;

@SpringBootTest
@Transactional
class ProductServiceTests {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("Spring context loads ProductService bean")
    void contextLoads() {
        assertNotNull(productService, "ProductService should be injected by Spring");
    }

    @Test
    @DisplayName("Save product with unique barcode persists correctly")
    void testSaveProductWithUniqueBarcode() {
        String uniqueBarcode = "TEST-" + UUID.randomUUID();

        Product product = new Product();
        product.setBarcode(uniqueBarcode);
        product.setName("Test Product");
        product.setKhmerName("Test KhmerName");
        product.setBuyPrice(new BigDecimal("10.00"));
        product.setSalePrice(new BigDecimal("15.00"));
        product.setAvailableUnit(BigDecimal.valueOf(100));
        product.setCategoryName("Diaper");

        Product savedProduct = productService.save(product);

        assertAll(
            () -> assertNotNull(savedProduct.getId(), "ID should be generated"),
            () -> assertEquals(uniqueBarcode, savedProduct.getBarcode()),
            () -> assertEquals("Test Product", savedProduct.getName()),
            () -> assertEquals("Test KhmerName", savedProduct.getKhmerName()),
            () -> assertEquals(new BigDecimal("10.00"), savedProduct.getBuyPrice()),
            () -> assertEquals(new BigDecimal("15.00"), savedProduct.getSalePrice()),
            () -> assertEquals(BigDecimal.valueOf(100), savedProduct.getAvailableUnit()),
            () -> assertEquals("Diaper", savedProduct.getCategoryName())
        );
    }

    @Test
    @DisplayName("Find product by barcode returns correct entity")
    void testFindByBarcode() {
        String barcode = "TEST-" + UUID.randomUUID();

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Lookup Product");
        product.setKhmerName("Lookup KhmerName");
        product.setBuyPrice(new BigDecimal("20.00"));
        product.setSalePrice(new BigDecimal("30.00"));
        product.setAvailableUnit(BigDecimal.valueOf(50));
        product.setCategoryName("Milk");

        productService.save(product);

        Product found = productService.getByBarcode(barcode);

        assertAll(
            () -> assertNotNull(found, "Product should be found by barcode"),
            () -> assertEquals("Lookup Product", found.getName()),
            () -> assertEquals(BigDecimal.valueOf(50), found.getAvailableUnit())
        );
    }

    @Test
    @DisplayName("Update existing product modifies fields correctly")
    void testUpdateProduct() {
        String barcode = "TEST-" + UUID.randomUUID();

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Original Product");
        product.setKhmerName("Original KhmerName");
        product.setBuyPrice(new BigDecimal("5.00"));
        product.setSalePrice(new BigDecimal("8.00"));
        product.setAvailableUnit(BigDecimal.valueOf(20));
        product.setCategoryName("Snacks");

        Product saved = productService.save(product);

        saved.setName("Updated Product");
        saved.setKhmerName("Updated KhmerName");
        saved.setSalePrice(new BigDecimal("9.00"));
        Product updated = productService.save(saved);

        assertAll(
            () -> assertEquals("Updated Product", updated.getName()),
            () -> assertEquals(new BigDecimal("9.00"), updated.getSalePrice()),
            () -> assertEquals(BigDecimal.valueOf(20), updated.getAvailableUnit())
        );
    }

    @Test
    @DisplayName("Delete product removes it from repository")
    void testDeleteProduct() {
        String barcode = "TEST-" + UUID.randomUUID();

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Delete Product");
        product.setKhmerName("Delete KhmerName");
        product.setBuyPrice(new BigDecimal("12.00"));
        product.setSalePrice(new BigDecimal("18.00"));
        product.setAvailableUnit(BigDecimal.valueOf(10));
        product.setCategoryName("Toys");

        Product saved = productService.save(product);
        assertNotNull(saved.getId(), "Product should be persisted before deletion");

        productService.deleteByBarcode(barcode);

        assertThrows(ResourceNotFoundException.class, () -> productService.getByBarcode(barcode));
    }

    @Test
    @DisplayName("Save product with fractional availableUnit persists correctly")
    void testSaveProductWithFractionalUnit() {
        String barcode = "TEST-" + UUID.randomUUID();

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Fractional Product");
        product.setKhmerName("Fractional KhmerName");
        product.setBuyPrice(new BigDecimal("2.50"));
        product.setSalePrice(new BigDecimal("3.75"));
        product.setAvailableUnit(BigDecimal.valueOf(1.5));
        product.setCategoryName("WeightBased");

        Product saved = productService.save(product);

        assertEquals(BigDecimal.valueOf(1.5), saved.getAvailableUnit());
    }
}

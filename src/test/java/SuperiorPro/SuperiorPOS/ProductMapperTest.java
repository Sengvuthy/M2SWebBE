package SuperiorPro.SuperiorPOS;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import SuperiorPro.SuperiorPOS.DTO.ProductImportItem;
import SuperiorPro.SuperiorPOS.entity.ProductImport;
import SuperiorPro.SuperiorPOS.mapper.ProductImportMapper;

class ProductMapperTest {

    private final ProductImportMapper mapper = Mappers.getMapper(ProductImportMapper.class);

    @Test
    @DisplayName("Map ProductImportItem → ProductImport entity correctly")
    void shouldMapItemToEntity() {
        ProductImportItem item = new ProductImportItem();
        item.setBarcode("1234567890");
        item.setProductName("Sample Product");
        item.setKhmerName("Khmer Product");
        item.setImportUnit(BigDecimal.valueOf(10));   // ✅ BigDecimal
        item.setBuyPrice(BigDecimal.valueOf(99.99));
        item.setSalePrice(BigDecimal.valueOf(120.00));

        ProductImport entity = mapper.toEntity(item);
        entity.setImportDate(LocalDateTime.now().toLocalDate());
        entity.setImportTime(LocalDateTime.now().toLocalTime());

        assertAll(
            () -> assertEquals("1234567890", entity.getBarcode()),
            () -> assertEquals("Sample Product", entity.getProductName()),
            () -> assertEquals("Khmer Product", entity.getKhmerName()),
            () -> assertEquals(BigDecimal.valueOf(10), entity.getImportUnit()), // ✅ compare BigDecimal
            () -> assertEquals(BigDecimal.valueOf(99.99), entity.getBuyPrice()),
            () -> assertEquals(BigDecimal.valueOf(99.99).multiply(BigDecimal.valueOf(10)), entity.getBuyAmount()),
            () -> assertEquals(BigDecimal.valueOf(120.00), entity.getSalePrice())
        );
    }

    @Test
    @DisplayName("Map ProductImport entity → ProductImportItem correctly")
    void shouldMapEntityToItem() {
        ProductImport entity = new ProductImport();
        entity.setBarcode("9876543210");
        entity.setProductName("Mapped Product");
        entity.setKhmerName("Mapped Khmer");
        entity.setImportUnit(BigDecimal.valueOf(5));   // ✅ BigDecimal
        entity.setBuyPrice(BigDecimal.valueOf(50.00));
        entity.setBuyAmount(BigDecimal.valueOf(250.00));
        entity.setSalePrice(BigDecimal.valueOf(75.00));
        entity.setImportDate(LocalDateTime.now().toLocalDate());
        entity.setImportTime(LocalDateTime.now().toLocalTime());

        ProductImportItem item = mapper.toItem(entity);

        assertAll(
            () -> assertEquals("9876543210", item.getBarcode()),
            () -> assertEquals("Mapped Product", item.getProductName()),
            () -> assertEquals("Mapped Khmer", item.getKhmerName()),
            () -> assertEquals(BigDecimal.valueOf(5), item.getImportUnit()), // ✅ compare BigDecimal
            () -> assertEquals(BigDecimal.valueOf(50.00), item.getBuyPrice()),
            () -> assertEquals(BigDecimal.valueOf(75.00), item.getSalePrice())
        );
    }
}
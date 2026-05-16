package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import SuperiorPro.SuperiorPOS.DTO.ProductDTO;
import SuperiorPro.SuperiorPOS.DTO.ProductImportItem;
import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.entity.ProductImport;
import SuperiorPro.SuperiorPOS.service.util.ProductMapperDecorator;

@Mapper(componentModel = "spring")
@DecoratedWith(ProductMapperDecorator.class)
public interface ProductMapper {

    Product toProduct(ProductDTO dto);

    @Mapping(target = "categoryName", source = "categoryName")
    @Mapping(target = "supplierId", source = "supplierId")
    ProductDTO toProductDTO(Product product);

    @Mapping(target = "barcode", source = "barcode")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "khmerName", source = "khmerName")
    @Mapping(target = "importUnit", source = "importUnit")
    @Mapping(target = "buyPrice", source = "buyPrice")
    @Mapping(target = "salePrice", source = "salePrice")
    @Mapping(target = "importDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    ProductImport toEntity(ProductImportItem item);

    ProductImportItem toItem(ProductImport entity);
}

package SuperiorPro.SuperiorPOS.mapper;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import SuperiorPro.SuperiorPOS.DTO.ProductImportDTO;
import SuperiorPro.SuperiorPOS.DTO.ProductImportItem;
import SuperiorPro.SuperiorPOS.entity.ProductImport;

@Mapper(componentModel = "spring")
public interface ProductImportMapper {

    ProductImportDTO toDTO(ProductImport productImport);

    ProductImport toEntity(ProductImportDTO productImportDTO);

    List<ProductImportDTO> toDTOs(List<ProductImport> productImports);

    List<ProductImport> toEntities(List<ProductImportDTO> productImportDTOs);

    ProductImportItem toItem(ProductImport entity);

    ProductImport toEntity(ProductImportItem item);

    List<ProductImportItem> toItems(List<ProductImport> productImports);

    List<ProductImport> toEntitiesFromItems(List<ProductImportItem> items);

    // 🔹 Automatically calculate buyAmount after mapping
    @AfterMapping
    default void calculateBuyAmount(ProductImportItem item, @MappingTarget ProductImport entity) {
        if (item.getBuyPrice() != null && item.getImportUnit() != null) {
            entity.setBuyAmount(item.getBuyPrice().multiply(item.getImportUnit()));
        }
    }
}

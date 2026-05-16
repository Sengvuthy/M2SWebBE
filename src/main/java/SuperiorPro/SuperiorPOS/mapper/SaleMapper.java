package SuperiorPro.SuperiorPOS.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import SuperiorPro.SuperiorPOS.DTO.SaleDTO;
import SuperiorPro.SuperiorPOS.entity.Sale;

@Mapper(componentModel = "spring")
public interface SaleMapper {
	
    SaleDTO toDTO(Sale sale);
    Sale toEntity(SaleDTO saleDTO);

    // Add list mappings for convenience
    List<SaleDTO> toDTOs(List<Sale> sales);
    List<Sale> toEntities(List<SaleDTO> saleDTOs);
}

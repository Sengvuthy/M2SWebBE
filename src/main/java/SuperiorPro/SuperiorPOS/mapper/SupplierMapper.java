package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.Mapper;

import SuperiorPro.SuperiorPOS.DTO.SupplierDTO;
import SuperiorPro.SuperiorPOS.entity.Supplier;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
	
    SupplierDTO toDTO(Supplier supplier);
    Supplier toSupplier(SupplierDTO dto);
}

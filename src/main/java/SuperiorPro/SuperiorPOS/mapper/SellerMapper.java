package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import SuperiorPro.SuperiorPOS.DTO.SellerDTO;
import SuperiorPro.SuperiorPOS.entity.Seller;

@Mapper(componentModel = "spring")
public interface SellerMapper {

    SellerDTO toDTO(Seller seller);

    @Mapping(target = "employeeCode", expression = "java(dto.getEmployeeCode() != null && !dto.getEmployeeCode().isBlank() ? dto.getEmployeeCode() : null)")
    Seller toEntity(SellerDTO dto);
}

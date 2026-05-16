package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import SuperiorPro.SuperiorPOS.DTO.UserRoleDTO;
import SuperiorPro.SuperiorPOS.entity.UserRole;

@Mapper
public interface UserRoleMapper {

    UserRoleMapper INSTANCE = Mappers.getMapper(UserRoleMapper.class);

    @Mapping(source = "id.userId", target = "userId")
    @Mapping(source = "id.roleId", target = "roleId")
    UserRoleDTO toDTO(UserRole userRole);
    
    @Mapping(target = "id.userId", source = "userId")
    @Mapping(target = "id.roleId", source = "roleId")
    UserRole toEntity(UserRoleDTO dto);
}

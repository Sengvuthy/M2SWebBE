package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import SuperiorPro.SuperiorPOS.DTO.RoleDTO;
import SuperiorPro.SuperiorPOS.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    Role toRole(RoleDTO dto);
    RoleDTO toRoleDTO(Role role);
}

package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import SuperiorPro.SuperiorPOS.DTO.RolePermissionDTO;
import SuperiorPro.SuperiorPOS.entity.RolePermission;

@Mapper
public interface RolePermissionMapper {

    RolePermissionMapper INSTANCE = Mappers.getMapper(RolePermissionMapper.class);

    @Mapping(source = "id.roleId", target = "roleId")
    @Mapping(source = "id.permissionId", target = "permissionId")
    RolePermissionDTO toDTO(RolePermission rolePermission);
    
    @Mapping(target = "id.roleId", source = "roleId")
    @Mapping(target = "id.permissionId", source = "permissionId")
    RolePermission toEntity(RolePermissionDTO dto);
}

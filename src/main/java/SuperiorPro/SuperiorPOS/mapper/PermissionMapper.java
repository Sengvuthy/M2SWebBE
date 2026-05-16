package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import SuperiorPro.SuperiorPOS.DTO.PermissionDTO;
import SuperiorPro.SuperiorPOS.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionMapper INSTANCE = Mappers.getMapper(PermissionMapper.class);

    @Mapping(source = "id", target = "id") // ✅ map ID
    @Mapping(source = "permissionName", target = "permissionName")
    @Mapping(source = "description", target = "description")
    Permission toPermission(PermissionDTO dto);

    @Mapping(source = "id", target = "id") // ✅ map ID
    @Mapping(source = "permissionName", target = "permissionName")
    @Mapping(source = "description", target = "description")
    PermissionDTO toPermissionDTO(Permission permission);
}

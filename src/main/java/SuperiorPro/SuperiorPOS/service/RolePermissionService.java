package SuperiorPro.SuperiorPOS.service;

import java.util.List;

import org.springframework.data.domain.Page;

import SuperiorPro.SuperiorPOS.DTO.RolePermissionDTO;
import SuperiorPro.SuperiorPOS.entity.RolePermission;
import SuperiorPro.SuperiorPOS.entity.RolePermissionKey;

public interface RolePermissionService {
	
    RolePermission assignPermissionToRole(Long roleId, Long permissionId);
    void removePermissionFromRole(Long roleId, Long permissionId);

    List<RolePermissionDTO> getPermissionsByRole(Long roleId);
    List<RolePermissionDTO> getRolesByPermission(Long permissionId);

    boolean exists(RolePermissionKey key);
    List<RolePermissionDTO> assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    // 🔹 New pagination methods
    Page<RolePermissionDTO> getAllRolePermissions(int page, int size, String sortBy, String sortDir);
    Page<RolePermissionDTO> getPermissionsByRolePaged(Long roleId, int page, int size, String sortBy, String sortDir);
    Page<RolePermissionDTO> getRolesByPermissionPaged(Long permissionId, int page, int size, String sortBy, String sortDir);
}

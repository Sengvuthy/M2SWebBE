package SuperiorPro.SuperiorPOS.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import SuperiorPro.SuperiorPOS.DTO.PermissionDTO;
import SuperiorPro.SuperiorPOS.entity.Permission;

public interface PermissionService {

    Permission save(PermissionDTO dto);

    Permission getById(Long id);

    Page<Permission> getPermissions(Pageable pageable);

    Permission updateById(Long id, PermissionDTO dto);

    void deleteById(Long id);

    // 🔹 NEW: Non-paginated list of all permissions
    List<Permission> getAllPermissions();
}

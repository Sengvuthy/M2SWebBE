package SuperiorPro.SuperiorPOS.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import SuperiorPro.SuperiorPOS.entity.RolePermission;
import SuperiorPro.SuperiorPOS.entity.RolePermissionKey;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionKey> {

    // Existing
    List<RolePermission> findByIdRoleId(Long roleId);
    List<RolePermission> findByIdPermissionId(Long permissionId);

    boolean existsById(RolePermissionKey id);
    void deleteById(RolePermissionKey id);

    // 🔹 New pageable queries
    Page<RolePermission> findByIdRoleId(Long roleId, Pageable pageable);
    Page<RolePermission> findByIdPermissionId(Long permissionId, Pageable pageable);
}

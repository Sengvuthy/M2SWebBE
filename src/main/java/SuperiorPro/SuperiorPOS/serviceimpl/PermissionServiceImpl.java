package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.PermissionDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelPermissionExportService;
import SuperiorPro.SuperiorPOS.entity.Permission;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.mapper.PermissionMapper;
import SuperiorPro.SuperiorPOS.repository.PermissionRepository;
import SuperiorPro.SuperiorPOS.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final ExcelPermissionExportService excelPermissionExportService;

    @Override
    @Transactional
    public Permission save(PermissionDTO dto) {
        String rawName = dto.getPermissionName();

        if (rawName == null || rawName.trim().isEmpty()) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Permission name must not be null or blank");
        }

        Permission permission = PermissionMapper.INSTANCE.toPermission(dto);
        permission.setPermissionName(rawName.trim());
        permission.setDescription(dto.getDescription());

        Permission saved = permissionRepository.save(permission);
        log.info("✅ Created permission ID {} with name '{}'", saved.getId(), saved.getPermissionName());
        exportToExcel();
        return saved;
    }

    @Override
    public Permission getById(Long id) {
        log.debug("Fetching permission by ID: {}", id);
        return permissionRepository.findById(id)
                .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND,
                        "Permission with ID %d is not found".formatted(id)));
    }

    @Override
    public Page<Permission> getPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Permission updateById(Long id, PermissionDTO dto) {
        Permission permission = getById(id);

        String rawName = dto.getPermissionName();
        if (rawName == null || rawName.trim().isEmpty()) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Permission name must not be null or blank");
        }

        permission.setPermissionName(rawName.trim());
        permission.setDescription(dto.getDescription());

        Permission updated = permissionRepository.save(permission);
        log.info("✏️ Updated permission ID {} with name '{}'", updated.getId(), updated.getPermissionName());
        exportToExcel();
        return updated;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Permission permission = getById(id);
        log.info("🗑️ Deleting permission ID {} with name '{}'", permission.getId(), permission.getPermissionName());
        permissionRepository.delete(permission);
        exportToExcel();
    }

    // 🔹 NEW: Non-paginated list of all permissions
    @Override
    public List<Permission> getAllPermissions() {
        log.debug("Fetching all permissions (non-paginated)");
        return permissionRepository.findAll();
    }

    private void exportToExcel() {
        try {
            excelPermissionExportService.exportPermissionsToExcel();
        } catch (IOException e) {
            log.error("❌ Failed to export permissions after mutation", e);
        }
    }
}

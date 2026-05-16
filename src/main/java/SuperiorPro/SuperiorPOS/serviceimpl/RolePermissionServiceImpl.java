package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.RolePermissionDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRolePermissionExportService;
import SuperiorPro.SuperiorPOS.entity.Permission;
import SuperiorPro.SuperiorPOS.entity.Role;
import SuperiorPro.SuperiorPOS.entity.RolePermission;
import SuperiorPro.SuperiorPOS.entity.RolePermissionKey;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.exception.ResourceNotFoundException;
import SuperiorPro.SuperiorPOS.mapper.RolePermissionMapper;
import SuperiorPro.SuperiorPOS.repository.PermissionRepository;
import SuperiorPro.SuperiorPOS.repository.RolePermissionRepository;
import SuperiorPro.SuperiorPOS.repository.RoleRepository;
import SuperiorPro.SuperiorPOS.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ExcelRolePermissionExportService excelRolePermissionExportService;

    @Override
    @Transactional
    public RolePermission assignPermissionToRole(Long roleId, Long permissionId) {
        RolePermissionKey key = new RolePermissionKey(roleId, permissionId);

        if (rolePermissionRepository.existsById(key)) {
            throw new API_Exception(HttpStatus.CONFLICT, "Permission already assigned to role");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", permissionId.toString()));

        RolePermission rp = new RolePermission();
        rp.setId(key);
        rp.setRoleName(role.getRoleName());
        rp.setPermissionName(permission.getPermissionName());
        rp.setRole(role);
        rp.setPermission(permission);

        RolePermission saved = rolePermissionRepository.save(rp);
        log.info("Assigned permission '{}' to role '{}'", permission.getPermissionName(), role.getRoleName());
        exportToExcel();
        return saved;
    }

    @Override
    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        RolePermissionKey key = new RolePermissionKey(roleId, permissionId);

        if (!rolePermissionRepository.existsById(key)) {
            throw new API_Exception(HttpStatus.NOT_FOUND, "Role-permission mapping not found");
        }
        
        exportToExcel();
        rolePermissionRepository.deleteById(key);
        log.info("Removed permission ID {} from role ID {}", permissionId, roleId);
    }

    @Override
    public List<RolePermissionDTO> getPermissionsByRole(Long roleId) {
        return rolePermissionRepository.findByIdRoleId(roleId).stream()
                .map(RolePermissionMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RolePermissionDTO> getRolesByPermission(Long permissionId) {
        return rolePermissionRepository.findByIdPermissionId(permissionId).stream()
                .map(RolePermissionMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RolePermissionDTO> getAllRolePermissions(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RolePermission> rolePermissionPage = rolePermissionRepository.findAll(pageable);
        return rolePermissionPage.map(RolePermissionMapper.INSTANCE::toDTO);
    }

    @Override
    public Page<RolePermissionDTO> getPermissionsByRolePaged(Long roleId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RolePermission> rolePermissionPage = rolePermissionRepository.findByIdRoleId(roleId, pageable);
        return rolePermissionPage.map(RolePermissionMapper.INSTANCE::toDTO);
    }

    @Override
    public Page<RolePermissionDTO> getRolesByPermissionPaged(Long permissionId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RolePermission> rolePermissionPage = rolePermissionRepository.findByIdPermissionId(permissionId, pageable);
        return rolePermissionPage.map(RolePermissionMapper.INSTANCE::toDTO);
    }


    @Override
    public boolean exists(RolePermissionKey key) {
        return rolePermissionRepository.existsById(key);
    }

    @Override
    @Transactional
    public List<RolePermissionDTO> assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        List<RolePermission> resultMappings = new ArrayList<>();

        for (Permission permission : permissions) {
            RolePermissionKey key = new RolePermissionKey(roleId, permission.getId());
            RolePermission rp;

            if (rolePermissionRepository.existsById(key)) {
                rp = rolePermissionRepository.findById(key).orElse(null);
            } else {
                rp = new RolePermission();
                rp.setId(key);
                rp.setRoleName(role.getRoleName());
                rp.setPermissionName(permission.getPermissionName());
                rp.setRole(role);
                rp.setPermission(permission);
                rp = rolePermissionRepository.save(rp);
            }

            if (rp != null) resultMappings.add(rp);
        }

        log.info("Processed {} permissions for role '{}'", resultMappings.size(), role.getRoleName());
        
        exportToExcel();
        return resultMappings.stream()
                .map(RolePermissionMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    private void exportToExcel() {
        try {
            excelRolePermissionExportService.exportRolePermissionsToExcel();
        } catch (IOException e) {
            log.error("Failed to export role_permissions after mutation", e);
        }
    }
}

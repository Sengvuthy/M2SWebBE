package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.RoleDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRoleExportService;
import SuperiorPro.SuperiorPOS.entity.Role;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.mapper.RoleMapper;
import SuperiorPro.SuperiorPOS.repository.RoleRepository;
import SuperiorPro.SuperiorPOS.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final ExcelRoleExportService excelRoleExportService;

    @Override
    @Transactional
    public Role save(RoleDTO dto) {
        String trimmedName = dto.getRoleName().trim();

        Role role = RoleMapper.INSTANCE.toRole(dto);
        role.setRoleName(trimmedName);
        role.setDescription(dto.getDescription());

        Role saved = roleRepository.save(role);
        log.info("✅ Created role ID {} with name '{}'", saved.getId(), saved.getRoleName());
        exportToExcel();
        return saved;
    }

    @Override
    public Role getById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND,
                        "Role with ID %d is not found".formatted(id)));
    }

    @Override
    public Page<Role> getRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Role updateById(Long id, RoleDTO dto) {
        Role role = getById(id);
        role.setRoleName(dto.getRoleName().trim());
        role.setDescription(dto.getDescription());

        Role updated = roleRepository.save(role);
        log.info("✏️ Updated role ID {} with name '{}'", updated.getId(), updated.getRoleName());
        exportToExcel();
        return updated;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Role role = getById(id);
        roleRepository.delete(role);
        log.info("🗑️ Deleted role ID {} with name '{}'", role.getId(), role.getRoleName());
        exportToExcel();
    }

    private void exportToExcel() {
        try {
            excelRoleExportService.exportRolesToExcel();
        } catch (IOException e) {
            log.error("❌ Failed to export roles after mutation", e);
        }
    }
}

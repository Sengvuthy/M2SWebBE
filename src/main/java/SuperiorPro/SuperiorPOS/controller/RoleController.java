package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.DTO.PageDTO;
import SuperiorPro.SuperiorPOS.DTO.RoleDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRoleExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRoleImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRoleImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.entity.Role;
import SuperiorPro.SuperiorPOS.mapper.RoleMapper;
import SuperiorPro.SuperiorPOS.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final ExcelRoleExportService excelRoleExportService;
    private final ExcelRoleImportService excelRoleImportService;

    // ✅ Create
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        log.info("Creating role: {}", roleDTO.getRoleName());
        Role role = roleService.save(roleDTO);
        return ResponseEntity.created(URI.create("/roles/" + role.getId()))
                             .body(RoleMapper.INSTANCE.toRoleDTO(role));
    }

    // ✅ Read by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        log.info("Fetching role by ID: {}", id);
        Role role = roleService.getById(id);
        return ResponseEntity.ok(RoleMapper.INSTANCE.toRoleDTO(role));
    }

    // ✅ Update by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRoleById(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        log.info("Updating role by ID: {}", id);
        Role updatedRole = roleService.updateById(id, roleDTO);
        return ResponseEntity.ok(RoleMapper.INSTANCE.toRoleDTO(updatedRole));
    }

    // ✅ Search (ID‑based pagination only)
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<PageDTO<RoleDTO>> searchRoles(
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "sort", defaultValue = "id,asc") String sort
    ) {
        String[] sortParts = sort.split(",");
        if (sortParts.length != 2) {
            throw new IllegalArgumentException("Invalid sort format. Use 'field,direction'");
        }

        String sortField = sortParts[0];
        Sort.Direction direction = Sort.Direction.fromString(sortParts[1]);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.by(sortField).with(direction)));

        log.info("Searching roles: page {}, size {}, sort {} {}", page, size, sortField, direction);
        Page<Role> roles = roleService.getRoles(pageable);
        PageDTO<RoleDTO> pageDTO = new PageDTO<>(roles.map(RoleMapper.INSTANCE::toRoleDTO));
        return ResponseEntity.ok(pageDTO);
    }

    // ✅ Delete by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoleById(@PathVariable Long id) {
        log.info("Deleting role by ID: {}", id);
        roleService.deleteById(id);
        return ResponseEntity.ok("Role deleted successfully!");
    }

    // 📥 Import from Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<String> importRolesFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Roles");
            ImportSummary summary = excelRoleImportService.importRolesFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("✅ Imported roles from: ").append(filePath).append("\n")
                .append("🔄 Updated: ").append(summary.updated()).append("\n")
                .append("✅ Created: ").append(summary.created()).append("\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("Import failed", e);
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }
}

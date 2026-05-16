package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;

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
import SuperiorPro.SuperiorPOS.DTO.PermissionDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelPermissionExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelPermissionImportService;
import SuperiorPro.SuperiorPOS.entity.Permission;
import SuperiorPro.SuperiorPOS.mapper.PermissionMapper;
import SuperiorPro.SuperiorPOS.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final ExcelPermissionExportService excelPermissionExportService;
    private final ExcelPermissionImportService excelPermissionImportService;

    // ✅ Create Permission
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<PermissionDTO> createPermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        log.info("Creating permission: {}", permissionDTO.getPermissionName());
        Permission permission = permissionService.save(permissionDTO);
        return ResponseEntity
            .created(URI.create("/permissions/" + permission.getId()))
            .body(PermissionMapper.INSTANCE.toPermissionDTO(permission));
    }

    // ✅ Get Permission by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PermissionDTO> getPermissionById(@PathVariable Long id) {
        log.info("Fetching permission by ID: {}", id);
        Permission permission = permissionService.getById(id);
        return ResponseEntity.ok(PermissionMapper.INSTANCE.toPermissionDTO(permission));
    }
    
 // ✅ Get all permissions (non-paginated, for dropdowns in frontend)
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        log.info("Fetching all permissions (non-paginated)");
        List<Permission> permissions = permissionService.getAllPermissions();
        List<PermissionDTO> dtos = permissions.stream()
            .map(PermissionMapper.INSTANCE::toPermissionDTO)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    // ✅ Update Permission by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PermissionDTO> updatePermissionById(@PathVariable Long id,
                                                              @Valid @RequestBody PermissionDTO permissionDTO) {
        log.info("Updating permission by ID: {}", id);
        Permission updated = permissionService.updateById(id, permissionDTO);
        return ResponseEntity.ok(PermissionMapper.INSTANCE.toPermissionDTO(updated));
    }

    // 🔎 Search Permissions (Paginated, ID-only)
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<PageDTO<PermissionDTO>> searchPermissions(
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

        log.info("Searching permissions: page {}, size {}, sort {} {}", page, size, sortField, direction);
        Page<Permission> permissions = permissionService.getPermissions(pageable);
        PageDTO<PermissionDTO> pageDTO = new PageDTO<>(permissions.map(PermissionMapper.INSTANCE::toPermissionDTO));
        return ResponseEntity.ok(pageDTO);
    }

    // ✅ Delete Permission by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePermissionById(@PathVariable Long id) {
        log.info("Deleting permission by ID: {}", id);
        permissionService.deleteById(id);
        return ResponseEntity.ok("Permission deleted successfully!");
    }

    // 📥 Import Permissions from Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<String> importPermissionsFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Permissions");
            ExcelPermissionImportService.ImportSummary summary = excelPermissionImportService.importPermissionsFromExcel(filePath);

            String response = String.format(
                "✅ Imported %d permissions from: %s\n🔄 Updated: %d\n⚠️ Errors: %d",
                summary.created(), filePath, summary.updated(), summary.errors().size()
            );

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }
}

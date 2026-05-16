package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.DTO.RolePermissionBatchDTO;
import SuperiorPro.SuperiorPOS.DTO.RolePermissionDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRolePermissionExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRolePermissionImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelRolePermissionImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.entity.RolePermissionKey;
import SuperiorPro.SuperiorPOS.mapper.RolePermissionMapper;
import SuperiorPro.SuperiorPOS.service.RolePermissionService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

	private final RolePermissionService rolePermissionService;
	private final ExcelRolePermissionExportService excelRolePermissionsExportService;
	private final ExcelRolePermissionImportService excelRolePermissionImportService;

	// ✅ Improvement 1: Single assignment endpoint
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@PostMapping("/assign")
	public ResponseEntity<RolePermissionDTO> assignSingle(@RequestBody RolePermissionDTO dto) {
		RolePermissionDTO saved = RolePermissionMapper.INSTANCE
				.toDTO(rolePermissionService.assignPermissionToRole(dto.getRoleId(), dto.getPermissionId()));
		return ResponseEntity.ok(saved);
	}

	// ✅ Improvement 2: Standardized JSON response for remove
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@DeleteMapping("/remove")
	public ResponseEntity<Map<String, String>> removePermissionFromRole(@RequestParam Long roleId,
			@RequestParam Long permissionId) {

		log.info("Removing permission ID {} from role ID {}", permissionId, roleId);
		rolePermissionService.removePermissionFromRole(roleId, permissionId);
		return ResponseEntity.ok(Map.of("message", "Permission removed successfully"));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/by-role/{roleId}")
	public ResponseEntity<List<RolePermissionDTO>> getPermissionsByRole(@PathVariable Long roleId) {
		log.info("Fetching permissions for role ID {}", roleId);
		List<RolePermissionDTO> dtos = rolePermissionService.getPermissionsByRole(roleId);
		return ResponseEntity.ok(dtos);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/by-permission/{permissionId}")
	public ResponseEntity<List<RolePermissionDTO>> getRolesByPermission(@PathVariable Long permissionId) {
		log.info("Fetching roles for permission ID {}", permissionId);
		List<RolePermissionDTO> dtos = rolePermissionService.getRolesByPermission(permissionId);
		return ResponseEntity.ok(dtos);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/list")
	public ResponseEntity<Page<RolePermissionDTO>> getAllRolePermissions(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id.roleId") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir) {
		log.info("Fetching role-permission mappings page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy,
				sortDir);
		Page<RolePermissionDTO> result = rolePermissionService.getAllRolePermissions(page, size, sortBy, sortDir);
		return ResponseEntity.ok(result);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/by-role/{roleId}/paged")
	public ResponseEntity<Page<RolePermissionDTO>> getPermissionsByRolePaged(@PathVariable Long roleId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id.permissionId") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir) {
		log.info("Fetching permissions for role ID {} with paging", roleId);
		Page<RolePermissionDTO> result = rolePermissionService.getPermissionsByRolePaged(roleId, page, size, sortBy,
				sortDir);
		return ResponseEntity.ok(result);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/by-permission/{permissionId}/paged")
	public ResponseEntity<Page<RolePermissionDTO>> getRolesByPermissionPaged(@PathVariable Long permissionId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id.roleId") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir) {
		log.info("Fetching roles for permission ID {} with paging", permissionId);
		Page<RolePermissionDTO> result = rolePermissionService.getRolesByPermissionPaged(permissionId, page, size,
				sortBy, sortDir);
		return ResponseEntity.ok(result);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@PostMapping("/assign-batch")
	public ResponseEntity<List<RolePermissionDTO>> assignPermissionsBatch(
			@RequestBody RolePermissionBatchDTO batchDTO) {
		List<RolePermissionDTO> dtos = rolePermissionService.assignPermissionsToRole(batchDTO.getRoleId(),
				batchDTO.getPermissionIds());
		return ResponseEntity.ok(dtos);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
	@GetMapping("/exists")
	public ResponseEntity<Boolean> mappingExists(@RequestParam @NotNull Long roleId,
			@RequestParam @NotNull Long permissionId) {

		RolePermissionKey key = new RolePermissionKey(roleId, permissionId);
		boolean exists = rolePermissionService.exists(key);
		return ResponseEntity.ok(exists);
	}
	
    // ✅ Export role permissions to Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<String> exportRolePermissionsToExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("RolePermissions");
            int count = excelRolePermissionsExportService.exportRolePermissionsToExcel();

            log.info("📤 Exported {} role permissions to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " role permissions to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("❌ Export failed: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<String> importRolesPermissionFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("RolePermissions");
            ImportSummary summary = excelRolePermissionImportService.importRolesPermissionFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("✅ Imported role-permission\n")
                .append("🔄 Updated: ").append(summary.updated()).append("\n")
                .append("✅ Created: ").append(summary.created()).append("\n");

            if (!summary.errors().isEmpty()) {
                response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
                summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
            }

            log.info("📥 Import summary: {} updated, {} created, {} errors",
                summary.updated(), summary.created(), summary.errors().size());

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("❌ Import failed: " + e.getMessage());
        }
    }
}

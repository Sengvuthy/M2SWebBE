package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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

import SuperiorPro.SuperiorPOS.DTO.UserRoleBatchDTO;
import SuperiorPro.SuperiorPOS.DTO.UserRoleDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserRoleExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserRoleImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserRoleImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.entity.UserRoleKey;
import SuperiorPro.SuperiorPOS.mapper.UserRoleMapper;
import SuperiorPro.SuperiorPOS.service.UserRoleService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;
    private final ExcelUserRoleExportService excelUserRoleExportService;
    private final ExcelUserRoleImportService excelUserRoleImportService;

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/assign")
    public ResponseEntity<List<UserRoleDTO>> assignMultiple(@RequestBody List<UserRoleDTO> dtos) {
        List<UserRoleDTO> saved = dtos.stream()
            .map(dto -> userRoleService.assignRoleToUser(dto.getUserId(), dto.getRoleId()))
            .map(UserRoleMapper.INSTANCE::toDTO)
            .collect(Collectors.toList());

        log.info("✅ Assigned {} user-role mappings", saved.size());
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/assign-batch")
    public ResponseEntity<List<UserRoleDTO>> assignRolesBatch(@RequestBody UserRoleBatchDTO batchDTO) {
        List<UserRoleDTO> assigned = batchDTO.getRoleIds().stream()
            .map(roleId -> userRoleService.assignRoleToUser(batchDTO.getUserId(), roleId))
            .map(UserRoleMapper.INSTANCE::toDTO)
            .collect(Collectors.toList());

        log.info("✅ Batch assigned {} roles to user ID {}", assigned.size(), batchDTO.getUserId());
        return ResponseEntity.ok(assigned);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeRoleFromUser(
            @RequestParam Long userId,
            @RequestParam Long roleId) {

        userRoleService.removeRoleFromUser(userId, roleId);
        log.info("🗑️ Removed role ID {} from user ID {}", roleId, userId);
        return ResponseEntity.ok("✅ Role removed from user");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<UserRoleDTO>> getRolesByUser(@PathVariable Long userId) {
        log.info("🔍 Fetching roles for user ID {}", userId);
        List<UserRoleDTO> dtos = userRoleService.getRolesByUser(userId);
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/by-role/{roleId}")
    public ResponseEntity<List<UserRoleDTO>> getUsersByRole(@PathVariable Long roleId) {
        log.info("🔍 Fetching users for role ID {}", roleId);
        List<UserRoleDTO> dtos = userRoleService.getUsersByRole(roleId);
        return ResponseEntity.ok(dtos);
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> getAllUserRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id.userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String searchName) {

        log.info("🔍 Fetching user-role mappings page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir, searchName);

        var resultPage = userRoleService.getAllUserRoles(page, size, sortBy, sortDir, searchName);

        return ResponseEntity.ok(resultPage);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/exists")
    public ResponseEntity<Boolean> mappingExists(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull Long roleId) {

        UserRoleKey key = new UserRoleKey(userId, roleId);
        boolean exists = userRoleService.exists(key);
        log.info("🔍 Mapping exists for user ID {} and role ID {}: {}", userId, roleId, exists);
        return ResponseEntity.ok(exists);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<String> importUserRolesFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("UsersRole");
            ImportSummary summary = excelUserRoleImportService.importUserRolesFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                .append("✅ Imported user-role mappings from: ").append(filePath).append("\n")
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
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<String> exportUserRolesToExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("UsersRole");
            excelUserRoleExportService.exportUserRolesToExcel();
            log.info("📤 Exported user-role mappings to {}", filePath);
            return ResponseEntity.ok("✅ Exported user-role mappings to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }
}

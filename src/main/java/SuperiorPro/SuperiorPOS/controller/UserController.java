package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import SuperiorPro.SuperiorPOS.DTO.PageDTO;
import SuperiorPro.SuperiorPOS.DTO.UserDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.entity.User;
import SuperiorPro.SuperiorPOS.exception.ErrorDTO;
import SuperiorPro.SuperiorPOS.mapper.UserMapper;
import SuperiorPro.SuperiorPOS.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final ExcelUserExportService excelUserExportService;
	private final ExcelUserImportService excelUserImportService;

	// ✅ Create User
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
	@PostMapping
	public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
		log.info("👤 Creating user: {}", userDTO.getUsername());
		User user = userService.save(userDTO);
		return ResponseEntity.created(URI.create("/users/" + user.getId())).body(UserMapper.INSTANCE.toUserDTO(user));
	}

	// ✅ Get User by ID
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
		log.info("🔍 Fetching user by ID: {}", id);
		User user = userService.getById(id);
		return ResponseEntity.ok(UserMapper.INSTANCE.toUserDTO(user));
	}

	// ✅ Get User by Name
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
	@GetMapping("/by-name/{name}")
	public ResponseEntity<UserDTO> getUserByName(@PathVariable String name) {
		log.info("🔍 Fetching user by name: {}", name);
		User user = userService.getByName(name);
		return ResponseEntity.ok(UserMapper.INSTANCE.toUserDTO(user));
	}

	// ✅ Update User by ID
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<UserDTO> updateUserById(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
		log.info("✏️ Updating user by ID: {}", id);
		User updatedUser = userService.updateById(id, userDTO);
		return ResponseEntity.ok(UserMapper.INSTANCE.toUserDTO(updatedUser));
	}

	// ✅ Update User by Name
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
	@PutMapping("/by-name/{name}")
	public ResponseEntity<UserDTO> updateUserByName(@PathVariable String name, @Valid @RequestBody UserDTO userDTO) {
		log.info("✏️ Updating user by name: {}", name);
		User updatedUser = userService.update(name, userDTO);
		return ResponseEntity.ok(UserMapper.INSTANCE.toUserDTO(updatedUser));
	}

	// ✅ Search Users (NOW USING MAP-BASED FILTERING)
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
	@GetMapping("/search")
	public ResponseEntity<?> searchUsers(@RequestParam Map<String, String> params) {

		log.info("🔎 Searching users with params: {}", params);

		try {
			// ✅ Add sorting defaults
			String sortBy = params.getOrDefault("_sortBy", "id");
			String sortDir = params.getOrDefault("_sortDir", "asc");

			params.put("_sortBy", sortBy);
			params.put("_sortDir", sortDir);

			Page<User> users = userService.getUsers(params);
			PageDTO<UserDTO> pageDTO = new PageDTO<>(users.map(UserMapper.INSTANCE::toUserDTO));
			return ResponseEntity.ok(pageDTO);

		} catch (Exception e) {
			log.error("❌ Search failed", e);
			return ResponseEntity.badRequest().body(new ErrorDTO("Invalid search parameters"));
		}
	}

	// ✅ Delete User by ID
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
		log.info("🗑️ Deleting user by ID: {}", id);
		userService.deleteById(id);
		return ResponseEntity.ok("✅ User deleted successfully!");
	}

	// ✅ Delete User by Name
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
	@DeleteMapping("/by-name/{name}")
	public ResponseEntity<String> deleteUserByName(@PathVariable String name) {
		log.info("🗑️ Deleting user by name: {}", name);
		userService.deleteByName(name);
		return ResponseEntity.ok("✅ User deleted successfully!");
	}

	// ✅ Import Users from Excel
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
	@PostMapping("/import")
	public ResponseEntity<String> importUsersFromExcel() {
		try {
			String filePath = ExcelPathResolver.resolveFixedPath("Users");
			ImportSummary summary = excelUserImportService.importUsersFromExcel(filePath);

			StringBuilder response = new StringBuilder().append("📥 Imported users from: ").append(filePath)
					.append("\n").append("🔄 Updated: ").append(summary.updated()).append("\n").append("✅ Created: ")
					.append(summary.created()).append("\n");

			if (!summary.errors().isEmpty()) {
				response.append("❌ Errors (").append(summary.errors().size()).append("):\n");
				summary.errors().forEach(error -> response.append("• ").append(error).append("\n"));
			}

			return ResponseEntity.ok(response.toString());

		} catch (IOException e) {
			log.error("❌ Import failed", e);
			return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
		}
	}

	// ✅ Export Users to Excel
	@PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
	@GetMapping("/export")
	public ResponseEntity<String> exportUsersToExcel() {
		try {
			String filePath = ExcelPathResolver.resolveFixedPath("Users");
			int count = excelUserExportService.exportUsersToExcel();
			return ResponseEntity.ok("✅ Exported " + count + " users to: " + filePath);
		} catch (IOException e) {
			log.error("❌ Export failed", e);
			return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
		}
	}
}

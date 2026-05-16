package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.UserDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserExportService;
import SuperiorPro.SuperiorPOS.config.spec.UserFilter;
import SuperiorPro.SuperiorPOS.config.spec.UserSpec;
import SuperiorPro.SuperiorPOS.entity.Role;
import SuperiorPro.SuperiorPOS.entity.User;
import SuperiorPro.SuperiorPOS.entity.UserRole;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.exception.ResourceNotFoundException;
import SuperiorPro.SuperiorPOS.repository.RoleRepository;
import SuperiorPro.SuperiorPOS.repository.UserRepository;
import SuperiorPro.SuperiorPOS.repository.UserRoleRepository;
import SuperiorPro.SuperiorPOS.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;
	private final ExcelUserExportService excelUserExportService;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public User save(UserDTO dto) {
	    String trimmedUsername = dto.getUsername().trim();

	    if (userRepository.existsByUserNameIgnoreCase(trimmedUsername)) {
	        throw new API_Exception(HttpStatus.CONFLICT, "Username already exists");
	    }

	    // Create base user
	    User user = new User();
	    user.setUserName(trimmedUsername.toLowerCase());
	    user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
	    user.setPhoneNumber(dto.getPhoneNumber());

	    // ✅ Assign roles if provided
	    if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
	        Set<UserRole> userRoles = dto.getRoles().stream()
	            .map(roleName -> {
	                Role role = roleRepository.findByRoleName(roleName)
	                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleName));
	                UserRole userRole = new UserRole();
	                userRole.setUser(user);
	                userRole.setRole(role);
	                return userRole;
	            })
	            .collect(Collectors.toSet());
	        user.setUserRoles(userRoles);
	    } else {
	        // ✅ Default to USER role if none provided
	        Role defaultRole = roleRepository.findByRoleName("USER")
	            .orElseThrow(() -> new ResourceNotFoundException("Role", "USER"));
	        UserRole userRole = new UserRole();
	        userRole.setUser(user);
	        userRole.setRole(defaultRole);
	        user.setUserRoles(Set.of(userRole));
	    }

	    User savedUser = userRepository.save(user);
	    log.info("✅ Created user ID {} with roles {}", savedUser.getId(),
	             savedUser.getUserRoles().stream()
	                 .map(ur -> ur.getRole().getRoleName())
	                 .collect(Collectors.joining(", ")));

	    exportToExcel();
	    return savedUser;
	}

	@Override
	@Transactional(readOnly = true)
	public User getById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND, "User with ID %d not found".formatted(id)));
	}

	@Override
	@Transactional(readOnly = true)
	public User getByName(String name) {
		String trimmedName = name.trim();
		return userRepository.findByUserNameIgnoreCase(trimmedName)
				.orElseThrow(() -> new ResourceNotFoundException("User", trimmedName));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<User> getUsers(Map<String, String> params) {

		int page = Integer.parseInt(params.getOrDefault("_page", "1"));
		int limit = Integer.parseInt(params.getOrDefault("_limit", "10"));

		// ✅ Sorting
		String sortBy = params.getOrDefault("_sortBy", "id");
		String sortDir = params.getOrDefault("_sortDir", "asc");

		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

		Pageable pageable = PageRequest.of(page - 1, limit, sort);

		// ✅ Filtering
		UserFilter filter = new UserFilter();
		if (params.containsKey("name")) {
			filter.setUsername(params.get("name"));
		}
		if (params.containsKey("id")) {
			filter.setId(Long.parseLong(params.get("id")));
		}

		UserSpec spec = new UserSpec(filter);

		return userRepository.findAll(spec, pageable);
	}

	@Override
	@Transactional
	public User updateById(Long id, UserDTO dto) {
		User user = getById(id);
		user.setUserName(dto.getUsername().trim());
		user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
		user.setPhoneNumber(dto.getPhoneNumber());

		User updated = userRepository.save(user);
		log.info("🔄 Updated user ID {} with new encrypted password", updated.getId());
		exportToExcel();
		return updated;
	}

	@Override
	@Transactional
	public User update(String name, UserDTO dto) {
		User user = getByName(name);
		user.setUserName(dto.getUsername().trim());
		user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
		user.setPhoneNumber(dto.getPhoneNumber());

		User updated = userRepository.save(user);
		log.info("🔄 Updated user '{}' with new encrypted password", updated.getUserName());
		exportToExcel();
		return updated;
	}

	@Override
	@Transactional
	public void deleteById(Long userId) {
		List<UserRole> roles = userRoleRepository.findByIdUserId(userId);

		boolean isOwner = roles.stream().anyMatch(role -> "OWNER".equalsIgnoreCase(role.getRole().getRoleName()));

		if (isOwner) {
			long ownerCount = userRoleRepository.countByRoleRoleName("OWNER");
			if (ownerCount <= 1) {
				log.warn("⚠️ Attempt to delete the last OWNER user (ID: {})", userId);
				throw new API_Exception(HttpStatus.FORBIDDEN, "❌ Cannot delete the last OWNER user.");
			}
		}

		userRepository.deleteById(userId);
		log.info("✅ Deleted user ID {}", userId);
	}

	@Override
	@Transactional
	public void deleteByName(String userName) {
		String trimmedName = userName.trim();
		User user = userRepository.findByUserName(trimmedName)
				.orElseThrow(() -> new ResourceNotFoundException("User", trimmedName));

		Long userId = user.getId();
		List<UserRole> roles = userRoleRepository.findByIdUserId(userId);

		boolean isOwner = roles.stream().anyMatch(role -> "OWNER".equalsIgnoreCase(role.getRole().getRoleName()));

		if (isOwner) {
			long ownerCount = userRoleRepository.countByRoleRoleName("OWNER");
			if (ownerCount <= 1) {
				log.warn("⚠️ Attempt to delete the last OWNER user (name: {})", trimmedName);
				throw new API_Exception(HttpStatus.FORBIDDEN, "❌ Cannot delete the last OWNER user.");
			}
		}

		roles.forEach(
				role -> log.debug("🧾 Removing role '{}' from user '{}'", role.getRole().getRoleName(), trimmedName));
		userRoleRepository.deleteAll(roles);
		log.info("🧹 Deleted {} role mappings for user '{}'", roles.size(), trimmedName);

		userRepository.deleteById(userId);
		log.info("✅ Deleted user '{}'", trimmedName);
	}

	private void exportToExcel() {
		try {
			int count = excelUserExportService.exportUsersToExcel();
			log.info("📤 Exported {} users to Excel", count);
		} catch (IOException e) {
			log.error("❌ Failed to export users after mutation", e);
		}
	}
}

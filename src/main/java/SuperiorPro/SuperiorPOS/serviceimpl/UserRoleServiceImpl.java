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

import SuperiorPro.SuperiorPOS.DTO.UserRoleDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelUserRoleExportService;
import SuperiorPro.SuperiorPOS.entity.Role;
import SuperiorPro.SuperiorPOS.entity.User;
import SuperiorPro.SuperiorPOS.entity.UserRole;
import SuperiorPro.SuperiorPOS.entity.UserRoleKey;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.exception.ResourceNotFoundException;
import SuperiorPro.SuperiorPOS.mapper.UserRoleMapper;
import SuperiorPro.SuperiorPOS.repository.RoleRepository;
import SuperiorPro.SuperiorPOS.repository.UserRepository;
import SuperiorPro.SuperiorPOS.repository.UserRoleRepository;
import SuperiorPro.SuperiorPOS.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ExcelUserRoleExportService excelUserRoleExportService;

    @Override
    @Transactional
    public UserRole assignRoleToUser(Long userId, Long roleId) {
        UserRoleKey key = new UserRoleKey(userId, roleId);

        if (userRoleRepository.existsById(key)) {
            throw new API_Exception(HttpStatus.CONFLICT, "Role already assigned to user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));

        UserRole userRole = new UserRole();
        userRole.setId(key);
        userRole.setUserName(user.getUserName());
        userRole.setRoleName(role.getRoleName());
        userRole.setUser(user);
        userRole.setRole(role);

        UserRole saved = userRoleRepository.save(userRole);
        log.info("✅ Assigned role '{}' to user '{}'", role.getRoleName(), user.getUserName());
        exportToExcel();
        return saved;
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        UserRoleKey key = new UserRoleKey(userId, roleId);

        if (!userRoleRepository.existsById(key)) {
            throw new API_Exception(HttpStatus.NOT_FOUND, "User-role mapping not found");
        }

        UserRole userRole = userRoleRepository.findById(key)
                .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND,
                        "User-role mapping not found for userId=%d and roleId=%d".formatted(userId, roleId)));

        userRoleRepository.delete(userRole);
        log.info("🗑️ Removed role ID {} from user ID {}", roleId, userId);
        exportToExcel();
    }

    @Override
    public List<UserRoleDTO> getRolesByUser(Long userId) {
        return userRoleRepository.findByIdUserId(userId).stream()
                .map(UserRoleMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserRoleDTO> getUsersByRole(Long roleId) {
        return userRoleRepository.findByIdRoleId(roleId).stream()
                .map(UserRoleMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<UserRoleDTO> getAllUserRoles(int page, int size, String sortBy, String sortDir, String searchName) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserRole> userRolePage;

        if (searchName != null && !searchName.trim().isEmpty()) {
            userRolePage = userRoleRepository
                .findByUserNameContainingIgnoreCaseOrRoleNameContainingIgnoreCase(searchName.trim(), searchName.trim(), pageable);
        } else {
            userRolePage = userRoleRepository.findAll(pageable);
        }

        return userRolePage.map(UserRoleMapper.INSTANCE::toDTO);
    }

    @Override
    public boolean exists(UserRoleKey key) {
        return userRoleRepository.existsById(key);
    }

    @Override
    @Transactional
    public List<UserRoleDTO> assignRolesToUser(Long userId, List<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        List<Role> roles = roleRepository.findAllById(roleIds);
        List<UserRole> newMappings = new ArrayList<>();

        for (Role role : roles) {
            UserRoleKey key = new UserRoleKey(userId, role.getId());
            if (!userRoleRepository.existsById(key)) {
                UserRole userRole = new UserRole();
                userRole.setId(key);
                userRole.setUserName(user.getUserName());
                userRole.setRoleName(role.getRoleName());
                userRole.setUser(user);
                userRole.setRole(role);
                newMappings.add(userRole);
            }
        }

        List<UserRole> saved = userRoleRepository.saveAll(newMappings);
        log.info("✅ Batch assigned {} roles to user '{}'", saved.size(), user.getUserName());
        exportToExcel();

        return saved.stream()
                .map(UserRoleMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    private void exportToExcel() {
        try {
            excelUserRoleExportService.exportUserRolesToExcel();
        } catch (IOException e) {
            log.error("❌ Failed to export user-role mappings", e);
        }
    }
}

package SuperiorPro.SuperiorPOS.service;

import java.util.List;

import org.springframework.data.domain.Page;

import SuperiorPro.SuperiorPOS.DTO.UserRoleDTO;
import SuperiorPro.SuperiorPOS.entity.UserRole;
import SuperiorPro.SuperiorPOS.entity.UserRoleKey;

public interface UserRoleService {

	UserRole assignRoleToUser(Long userId, Long roleId);
    void removeRoleFromUser(Long userId, Long roleId);
    List<UserRoleDTO> getRolesByUser(Long userId);
    List<UserRoleDTO> getUsersByRole(Long roleId);
    boolean exists(UserRoleKey key);
    List<UserRoleDTO> assignRolesToUser(Long userId, List<Long> roleIds);
    
    Page<UserRoleDTO> getAllUserRoles(int page, int size, String sortBy, String sortDir, String searchName);
}

package SuperiorPro.SuperiorPOS.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.entity.UserRole;
import SuperiorPro.SuperiorPOS.entity.UserRoleKey;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleKey> {

    List<UserRole> findByIdUserId(Long userId);
    List<UserRole> findByIdRoleId(Long roleId);

    boolean existsById(UserRoleKey id);
    void deleteById(UserRoleKey id);

    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role.roleName = :roleName")
    long countByRoleRoleName(@Param("roleName") String roleName);

    // 🔹 New: pageable query
    Page<UserRole> findAll(Pageable pageable);

    // Optional: filter by userId with paging
    Page<UserRole> findByIdUserId(Long userId, Pageable pageable);

    // Optional: filter by roleId with paging
    Page<UserRole> findByIdRoleId(Long roleId, Pageable pageable);
    
    // for Search box in frontend
    Page<UserRole> findByUserNameContainingIgnoreCaseOrRoleNameContainingIgnoreCase(
    	    String userName, String roleName, Pageable pageable
    );
}

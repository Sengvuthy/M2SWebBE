package SuperiorPro.SuperiorPOS.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // ✅ Only ID-based operations remain
    Page<Role> findAll(Pageable pageable);
    Optional<Role> findByRoleName(String roleName);

    boolean existsById(Long id);
}

package SuperiorPro.SuperiorPOS.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // ✅ Only ID-based operations remain
    Page<Permission> findAll(Pageable pageable);

    boolean existsById(Long id);
}

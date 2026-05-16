package SuperiorPro.SuperiorPOS.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

	List<Supplier> findByActiveTrue();
    // For search (partial match by name)
    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Supplier> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    
    Page<Supplier> findByActiveTrue(Pageable pageable);
    Page<Supplier> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}

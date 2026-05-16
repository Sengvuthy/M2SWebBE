package SuperiorPro.SuperiorPOS.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import SuperiorPro.SuperiorPOS.entity.Seller;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByNameIgnoreCase(String name);

    Page<Seller> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByEmployeeCode(String employeeCode);
}

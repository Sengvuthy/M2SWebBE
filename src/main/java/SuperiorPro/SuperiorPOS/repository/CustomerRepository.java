package SuperiorPro.SuperiorPOS.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 🔍 Search by partial name (for pagination/filtering)
    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 🔍 Check existence by phone (search inside phones list)
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Customer c JOIN c.phones p WHERE p = :phone")
    boolean existsByPhone(@Param("phone") String phone);

    // 🔍 Find customer by phone (search inside phones list)
    @Query("SELECT c FROM Customer c JOIN c.phones p WHERE p = :phone")
    Optional<Customer> findByPhone(@Param("phone") String phone);
    
    boolean existsByTelegramId(Long telegramId);
    Optional<Customer> findByTelegramId(Long telegramId);
}

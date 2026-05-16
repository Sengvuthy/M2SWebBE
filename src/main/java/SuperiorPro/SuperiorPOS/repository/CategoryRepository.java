package SuperiorPro.SuperiorPOS.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	
    // Optional: still allow searching by name for convenience
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}

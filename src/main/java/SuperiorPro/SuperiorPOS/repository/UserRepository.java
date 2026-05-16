package SuperiorPro.SuperiorPOS.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import SuperiorPro.SuperiorPOS.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>{

	@Query("SELECT u FROM User u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameIgnoreCase(@Param("name") String name);
    
	@Query("SELECT u FROM User u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :name, '%'))")
	Page<User> findListByNameIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :name, '%'))")
    void deleteByName(@Param("name") String name);
    
    Optional<User> findByUserName(String userName);
    Optional<User> findByUserNameIgnoreCase(String userName);
    boolean existsByUserNameIgnoreCase(String name);
}

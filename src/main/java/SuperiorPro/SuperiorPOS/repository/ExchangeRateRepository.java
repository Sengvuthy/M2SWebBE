package SuperiorPro.SuperiorPOS.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import SuperiorPro.SuperiorPOS.entity.ExchangeRate;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
	
	Optional<ExchangeRate> findTopByOrderByIdDesc();
}

package SuperiorPro.SuperiorPOS.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import SuperiorPro.SuperiorPOS.entity.YearlySaleReport;

public interface YearlySaleReportRepository extends JpaRepository<YearlySaleReport, Long> {
	
    Optional<YearlySaleReport> findByReportYear(Integer year);
}

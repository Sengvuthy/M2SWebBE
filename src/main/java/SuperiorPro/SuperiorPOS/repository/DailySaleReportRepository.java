package SuperiorPro.SuperiorPOS.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import SuperiorPro.SuperiorPOS.entity.DailySaleReport;

public interface DailySaleReportRepository extends JpaRepository<DailySaleReport, Long> {

    Optional<DailySaleReport> findByReportDate(LocalDate reportDate);
    boolean existsByReportDate(LocalDate reportDate);
    List<DailySaleReport> findByReportDateBetween(LocalDate startDate, LocalDate endDate);
}

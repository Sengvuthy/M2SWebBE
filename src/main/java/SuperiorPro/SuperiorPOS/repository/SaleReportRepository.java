package SuperiorPro.SuperiorPOS.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import SuperiorPro.SuperiorPOS.entity.SaleReport;

public interface SaleReportRepository extends JpaRepository<SaleReport, Long> {
    
    Optional<SaleReport> findByReportDate(LocalDate reportDate);
    boolean existsByReportDate(LocalDate reportDate);

    // ✅ Needed for monthly aggregation
    List<SaleReport> findByReportDateBetween(LocalDate startDate, LocalDate endDate);
}

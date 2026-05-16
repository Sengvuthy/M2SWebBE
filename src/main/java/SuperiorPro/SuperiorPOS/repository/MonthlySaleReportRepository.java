package SuperiorPro.SuperiorPOS.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import SuperiorPro.SuperiorPOS.entity.MonthlySaleReport;

public interface MonthlySaleReportRepository extends JpaRepository<MonthlySaleReport, Long> {

    // ✅ Fetch a single month
    Optional<MonthlySaleReport> findByReportYearAndReportMonth(Integer year, Integer month);

    // ✅ Fast existence check for upsert logic
    boolean existsByReportYearAndReportMonth(Integer year, Integer month);

    // ✅ Remove a single month (useful for rebuilds)
    void deleteByReportYearAndReportMonth(Integer year, Integer month);

    // ✅ Fetch all months in a given year (ordered)
    List<MonthlySaleReport> findByReportYearOrderByReportMonthAsc(Integer year);

    // ✅ Fetch across years (for batch operations)
    List<MonthlySaleReport> findByReportYearBetweenOrderByReportYearAscReportMonthAsc(Integer startYear, Integer endYear);
}

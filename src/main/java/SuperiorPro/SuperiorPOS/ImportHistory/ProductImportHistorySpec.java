package SuperiorPro.SuperiorPOS.ImportHistory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

import SuperiorPro.SuperiorPOS.entity.ProductImport;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductImportHistorySpec implements Specification<ProductImport> {

    private final ProductImportHistoryFilter importFilter;

    @Override
    public Predicate toPredicate(Root<ProductImport> importHistory, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // ✅ Convert LocalDate to LocalDateTime for proper comparison
        if (Objects.nonNull(importFilter.getStartDate())) {
            LocalDateTime startDateTime = importFilter.getStartDate().atStartOfDay();
            predicates.add(cb.greaterThanOrEqualTo(importHistory.get("importDate"), startDateTime));
        }

        if (Objects.nonNull(importFilter.getEndDate())) {
            LocalDateTime endDateTime = importFilter.getEndDate().atTime(LocalTime.MAX);
            predicates.add(cb.lessThanOrEqualTo(importHistory.get("importDate"), endDateTime));
        }

        // ✅ Return conjunction (always true) if no filters are applied
        return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
    }
}

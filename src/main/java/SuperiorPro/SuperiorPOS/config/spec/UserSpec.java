package SuperiorPro.SuperiorPOS.config.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import SuperiorPro.SuperiorPOS.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;

@Data
public class UserSpec implements Specification<User> {

	private final UserFilter userFilter;
	List<Predicate> predicates​ = new ArrayList<Predicate>();

	@Override
	public Predicate toPredicate(Root<User> user, CriteriaQuery<?> query, CriteriaBuilder cb) {
		List<Predicate> predicates = new ArrayList<>();

		if (userFilter.getUsername() != null && !userFilter.getUsername().isBlank()) {
			predicates.add(cb.like(cb.lower(user.get("userName")), "%" + userFilter.getUsername().toLowerCase() + "%"));
		}

		if (userFilter.getId() != null) {
			predicates.add(cb.equal(user.get("id"), userFilter.getId()));
		}

		return cb.and(predicates.toArray(new Predicate[0]));
	}
}

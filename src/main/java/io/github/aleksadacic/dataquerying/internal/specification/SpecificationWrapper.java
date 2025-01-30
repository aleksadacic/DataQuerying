package io.github.aleksadacic.dataquerying.internal.specification;

import io.github.aleksadacic.dataquerying.api.SearchOperator;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationWrapper<T> implements Specification<T> {
    private final transient Filter filter;

    public SpecificationWrapper(Filter filter) {
        this.filter = filter;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (filter == null || isFilterInvalid()) {
            // Return null to avoid adding trivial predicates
            return null;
        }

        Path<?> fieldPath = SpecificationUtils.getPath(root, filter.getAttribute());
        return switch (filter.getOperator()) {
            case EQ -> SpecificationEngine.eq(filter, criteriaBuilder, fieldPath);
            case NOT_EQ -> SpecificationEngine.notEq(filter, criteriaBuilder, fieldPath);
            case GTE -> SpecificationEngine.gte(filter, criteriaBuilder, (Expression<? extends Comparable>) fieldPath);
            case LTE -> SpecificationEngine.lte(filter, criteriaBuilder, (Expression<? extends Comparable>) fieldPath);
            case GT -> SpecificationEngine.gt(filter, criteriaBuilder, (Expression<? extends Comparable>) fieldPath);
            case LT -> SpecificationEngine.lt(filter, criteriaBuilder, (Expression<? extends Comparable>) fieldPath);
            case LIKE -> SpecificationEngine.like(filter, criteriaBuilder, fieldPath);
            case NOT_LIKE -> SpecificationEngine.notLike(filter, criteriaBuilder, fieldPath);
            case BETWEEN ->
                    SpecificationEngine.between(filter, criteriaBuilder, (Expression<? extends Comparable>) fieldPath);
            case IN -> SpecificationEngine.in(filter, criteriaBuilder, fieldPath);
        };
    }

    private boolean isFilterInvalid() {
        return filter.getAttribute() == null ||
                (filter.getValue() == null && (filter.getOperator() != SearchOperator.EQ && filter.getOperator() != SearchOperator.NOT_EQ));
    }
}

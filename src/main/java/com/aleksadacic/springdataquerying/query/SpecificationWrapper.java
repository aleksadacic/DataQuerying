package com.aleksadacic.springdataquerying;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

class SpecificationWrapper<T> implements Specification<T> {
    private final Filter filter;

    public SpecificationWrapper(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        // Ensure the attribute and value are not null TODO
        if (filter.getAttribute() == null || filter.getValue() == null) {
            throw new IllegalArgumentException("Attribute and value cannot be null");
        }

        // Get the path of the attribute from the root
        Path<?> fieldPath = root.get(filter.getAttribute());

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
//    public void applyColumnSelector(Root<T> root, CriteriaQuery<?> query) {
//        if (filter.getColumns() != null && !filter.getColumns().isEmpty()) {
//            query.multiselect(filter.getColumns().stream().map(root::get).toArray(Expression[]::new));
//        }
//    }
}

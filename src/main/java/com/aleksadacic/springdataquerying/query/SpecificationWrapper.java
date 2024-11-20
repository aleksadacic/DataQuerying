package com.aleksadacic.springdataquerying.query;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

class SpecificationWrapper<T> implements Specification<T> {
    private final Filter filter;

    public SpecificationWrapper(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (filter == null) {
            return criteriaBuilder.conjunction();
        }

        if (isFilterInvalid()) {
            throw new IllegalArgumentException("Attribute cannot be null and value cannot be null unless the operation is EQ or NOT_EQ.");
        }

        Path<?> fieldPath = getPath(root, filter.getAttribute());
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
                (filter.getValue() == null && (filter.getOperator() != FilterOperator.EQ && filter.getOperator() != FilterOperator.NOT_EQ));
    }

    //TODO utils
    // Utility method to retrieve the correct Path based on attribute
    private Path<?> getPath(Root<T> root, String attribute) {
        if (attribute.contains(".")) {
            String[] parts = attribute.split("\\.");
            Join<?, ?> join = root.getJoins().stream()
                    .filter(e -> e.getAttribute().getName().equals(parts[0]))
                    .findFirst()
                    .orElse(null);
            if (join == null) {
                throw new IllegalArgumentException("Join not found for attribute: " + parts[0]);
            }

            Path<?> path = join;
            for (int i = 1; i < parts.length; i++) {
                path = path.get(parts[i]);
            }
            return path;
        } else {
            return root.get(attribute);
        }
    }

    //TODO utils
    private static <T> Join<?, ?> getJoinByName(Root<T> root, String joinName) {
        return root.getJoins().stream()
                .filter(join -> join.getAttribute().getName().equals(joinName))
                .findFirst()
                .orElse(null); // or throw an exception if you prefer
    }
}

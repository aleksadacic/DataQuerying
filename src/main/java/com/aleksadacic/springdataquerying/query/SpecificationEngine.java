package com.aleksadacic.springdataquerying.query;

import jakarta.persistence.criteria.*;

import java.util.Collection;
import java.util.List;

class SpecificationEngine {

    private SpecificationEngine() {
    }

    public static Predicate eq(Filter filter, CriteriaBuilder criteriaBuilder, Path<?> fieldPath) {
        if (filter.getValue() == null) {
            return criteriaBuilder.isNull(fieldPath);
        }
        return criteriaBuilder.equal(fieldPath, filter.getValue());
    }

    public static Predicate notEq(Filter filter, CriteriaBuilder criteriaBuilder, Path<?> fieldPath) {
        if (filter.getValue() == null) {
            return criteriaBuilder.isNotNull(fieldPath);
        }
        return criteriaBuilder.notEqual(fieldPath, filter.getValue());
    }

    public static Predicate gte(Filter filter, CriteriaBuilder criteriaBuilder, Expression<? extends Comparable> fieldPath) {
        return criteriaBuilder.greaterThanOrEqualTo(
                fieldPath,
                (Comparable) filter.getValue()
        );
    }

    public static CriteriaBuilder.In<Object> in(Filter filter, CriteriaBuilder criteriaBuilder, Path<?> fieldPath) {
        if (!(filter.getValue() instanceof Collection<?> values)) {
            throw new IllegalArgumentException("IN operator requires a collection of values");
        }
        CriteriaBuilder.In<Object> inClause = criteriaBuilder.in(fieldPath);
        for (Object value : values) {
            inClause.value(value);
        }
        return inClause;
    }

    public static Predicate between(Filter filter, CriteriaBuilder criteriaBuilder, Expression<? extends Comparable> fieldPath) {
        if (!(filter.getValue() instanceof List<?> valueList) || valueList.size() != 2) {
            throw new IllegalArgumentException("BETWEEN operator requires a list of two comparable values");
        }
        Comparable lowerBound = (Comparable) valueList.get(0);
        Comparable upperBound = (Comparable) valueList.get(1);
        return criteriaBuilder.between(
                fieldPath,
                lowerBound,
                upperBound
        );
    }

    public static Predicate notLike(Filter filter, CriteriaBuilder criteriaBuilder, Path<?> fieldPath) {
        if (!(filter.getValue() instanceof String value)) {
            throw new IllegalArgumentException("NOT_LIKE operator requires a String value");
        }
        return criteriaBuilder.notLike(fieldPath.as(String.class), "%" + value + "%");
    }

    public static Predicate like(Filter filter, CriteriaBuilder criteriaBuilder, Path<?> fieldPath) {
        if (!(filter.getValue() instanceof String value)) {
            throw new IllegalArgumentException("LIKE operator requires a String value");
        }
        return criteriaBuilder.like(fieldPath.as(String.class), "%" + value + "%");
    }

    public static Predicate lt(Filter filter, CriteriaBuilder criteriaBuilder, Expression<? extends Comparable> fieldPath) {
        return criteriaBuilder.lessThan(
                fieldPath,
                (Comparable) filter.getValue()
        );
    }

    public static Predicate gt(Filter filter, CriteriaBuilder criteriaBuilder, Expression<? extends Comparable> fieldPath) {
        return criteriaBuilder.greaterThan(
                fieldPath,
                (Comparable) filter.getValue()
        );
    }

    public static Predicate lte(Filter filter, CriteriaBuilder criteriaBuilder, Expression<? extends Comparable> fieldPath) {
        return criteriaBuilder.lessThanOrEqualTo(
                fieldPath,
                (Comparable) filter.getValue()
        );
    }

    // Utility method to apply the selected fields to the CriteriaQuery
    public static <T> void applySelection(Root<T> root, CriteriaQuery<?> query, List<String> selectedFields) {
        if (selectedFields != null && !selectedFields.isEmpty()) {
            query.multiselect(selectedFields.stream()
                    .map(root::get).toArray(Selection[]::new));
        }
    }
}

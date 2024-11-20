package com.aleksadacic.springdataquerying.data;

import com.aleksadacic.springdataquerying.query.FilterOperator;
import com.aleksadacic.springdataquerying.query.Query;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class QuerySpecificationBuilder<T> {

    public Specification<T> buildSpecification(QueryData<T> queryData) {
        Query<T> query = Query.get();

        // Apply top-level filters
        if (queryData.getFilters() != null) {
            for (FilterData filter : queryData.getFilters()) {
                applyFilter(query, filter);
            }
        }

        // Apply condition groups recursively
        if (queryData.getConditionGroups() != null) {
            for (ConditionGroup group : queryData.getConditionGroups()) {
                applyConditionGroup(query, group);
            }
        }

        // Build the specification and return
        return query.buildSpecification();
    }

    private void applyFilter(Query<T> query, FilterData filter) {
        if (filter.getField().contains(".")) {
            query.join(filter.getField().split("\\.")[0], JoinType.INNER);
        }

        // Depending on the operator, we apply the filter
        switch (filter.getOperator()) {
            case EQ -> query.and(filter.getField(), FilterOperator.EQ, filter.getValue());
            case NOT_EQ -> query.and(filter.getField(), FilterOperator.NOT_EQ, filter.getValue());
            case GTE -> query.and(filter.getField(), FilterOperator.GTE, filter.getValue());
            case LTE -> query.and(filter.getField(), FilterOperator.LTE, filter.getValue());
            case GT -> query.and(filter.getField(), FilterOperator.GT, filter.getValue());
            case LT -> query.and(filter.getField(), FilterOperator.LT, filter.getValue());
            case LIKE -> query.and(filter.getField(), FilterOperator.LIKE, filter.getValue());
            case NOT_LIKE -> query.and(filter.getField(), FilterOperator.NOT_LIKE, filter.getValue());
            case BETWEEN -> query.and(filter.getField(), FilterOperator.BETWEEN, filter.getValue());
            case IN -> query.and(filter.getField(), FilterOperator.IN, filter.getValue());
        }
    }

    private void applyConditionGroup(Query<T> query, ConditionGroup group) {
        // Create a temporary Query for nested conditions
        Query<T> nestedQuery = Query.get();

        // Apply filters within the group
        if (group.getFilters() != null) {
            for (FilterData filter : group.getFilters()) {
                applyFilter(nestedQuery, filter);
            }
        }

        // Apply nested condition groups recursively
        if (group.getNestedGroups() != null) {
            for (ConditionGroup nestedGroup : group.getNestedGroups()) {
                applyConditionGroup(nestedQuery, nestedGroup);
            }
        }

        // Combine this nested query back into the main query with AND or OR based on the logical operator
        if (group.getLogicalOperator() == LogicalOperator.AND) {
            query.and(nestedQuery);
        } else if (group.getLogicalOperator() == LogicalOperator.OR) {
            query.or(nestedQuery);
        }
    }
}

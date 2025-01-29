package com.aleksadacic.dataquerying.internal.search;

import com.aleksadacic.dataquerying.api.Query;
import com.aleksadacic.dataquerying.api.SearchOperator;
import com.aleksadacic.dataquerying.api.SearchRequest;
import com.aleksadacic.dataquerying.internal.enums.ConditionalOperator;
import jakarta.persistence.criteria.JoinType;

import java.util.List;

public class SearchRequestQueryTransformer {

    private SearchRequestQueryTransformer() {
    }

    public static <T> Query<T> toQuery(SearchRequest request) {
        // Start with an empty Query
        Query<T> query = Query.get();

        // Process top-level filter data if present
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            processFilters(query, request.getFilters(), request.getConditionalOperator());
        }

        return query;
    }

    // Recursively processes the list of filters and appends them to the Query.
    private static <T> void processFilters(Query<T> query,
                                           List<FilterData> filters,
                                           ConditionalOperator groupOperator) {
        for (FilterData filter : filters) {
            // If the filter has nested filters, it represents a group
            if (filter.getFilters() != null && !filter.getFilters().isEmpty()) {
                Query<T> subQuery = Query.get();
                processFilters(subQuery, filter.getFilters(), filter.getConditionalOperator());

                // Attach the subQuery to the main query with the correct group operator
                if (groupOperator == ConditionalOperator.AND) {
                    query.and(subQuery);
                } else {
                    query.or(subQuery);
                }

            } else {
                processLeafFilter(query, groupOperator, filter);
            }
        }
    }

    private static <T> void processLeafFilter(Query<T> query, ConditionalOperator groupOperator, FilterData filter) {
        String attribute = filter.getAttribute();
        SearchOperator operator = filter.getSearchOperator();
        Object value = filter.getValue();

        if (attribute.contains(".")) {
            query.join(filter.getAttribute().split("\\.")[0], JoinType.INNER);
            query.distinct();
        }
        // Example: if groupOperator == AND, then we do query.and(attribute, operator, value)
        // if groupOperator == OR, then we do query.or(attribute, operator, value)
        if (groupOperator == ConditionalOperator.AND) {
            query.and(attribute, operator, value);
        } else {
            query.or(attribute, operator, value);
        }
    }
}

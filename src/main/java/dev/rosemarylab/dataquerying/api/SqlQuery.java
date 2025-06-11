package dev.rosemarylab.dataquerying.api;


import jakarta.persistence.criteria.JoinType;

import java.util.List;

/**
 * A fluent SQL query builder.
 *
 * @param <T> the type of the result (for later mapping)
 */
public interface SqlQuery<T> {

    /**
     * Specify the columns (or expressions) to select.
     */
    SqlQuery<T> select(List<String> columns);

    /**
     * Specify the table to query from.
     */
    SqlQuery<T> from(String table);

    /**
     * Specify the table with alias to query from.
     */
    SqlQuery<T> from(String table, String alias);

    /**
     * Add a WHERE clause (can chain multiple where()/and()/or()).
     */
    SqlQuery<T> where(String condition, Object... params);

    /**
     * Add additional 'AND' condition.
     */
    SqlQuery<T> and(String condition, Object... params);

    /**
     * Add additional 'OR' condition.
     */
    SqlQuery<T> or(String condition, Object... params);

    /**
     * Add a join
     */
    SqlQuery<T> join(String tableWithAlias, JoinType joinType, String onCondition, Object... params);

    /**
     * Group the results by one or more columns.
     */
    SqlQuery<T> groupBy(String... columns);

    /**
     * Filter grouped results.
     */
    SqlQuery<T> having(String condition, Object... params);

    /**
     * Order the results.
     */
    SqlQuery<T> orderBy(String... columns);

    /**
     * Limit the number of results.
     */
    SqlQuery<T> limit(int maxRows);

    /**
     * Skip a number of results.
     */
    SqlQuery<T> offset(int startPosition);

    /**
     * Build and return the final SQL string.
     * (Optionally you could return a richer object with both SQL + params.)
     */
    String build();
}
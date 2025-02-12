package io.github.aleksadacic.dataquerying.api;

import io.github.aleksadacic.dataquerying.internal.specification.SpecificationQuery;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Query interface for building <code>org.springframework.data.jpa.domain.Specification</code> objects
 * with various conditions and joins. This interface provides static factory methods for creating
 * query instances and methods for adding conditions dynamically.
 * <br>
 * This interface is also available as a <b>Spring Bean</b> with a default implementation,
 * so you can use dependency injection to get the instance.
 * <p>Example usage:
 * <pre>
 * Query&lt;User&gt; query = SpecificationQuery.where("username", "john_doe")
 *     .and("age", SearchOperator.GT, 18)
 *     .join("profile.address", JoinType.LEFT)
 *     .distinct();
 *
 * Specification&lt;User&gt; spec = query.buildSpecification();
 * </pre>
 * </p>
 *
 * @param <T> The type of the entity being queried.
 */
public interface Query<T> {
    /**
     * Creates a new instance with no initial conditions.
     *
     * @param <T> the type of the entity being queried.
     * @return a new {@link Query} instance with an empty specification.
     */
    static <T> Query<T> get() {
        return SpecificationQuery.get();
    }

    /**
     * Creates a new instance with the provided {@link Specification}.
     *
     * @param specification the initial specification to apply to the query; may be {@code null}
     * @param <T>           the type of the entity being queried.
     * @return a new {@link Query} instance with the specified initial condition.
     */
    static <T> Query<T> get(Specification<T> specification) {
        return SpecificationQuery.get(specification);
    }

    /**
     * Creates a new {@link Query} instance based on an existing query.
     *
     * @param query The existing query to base the new query on.
     * @param <T>   The type of the entity being queried.
     * @return A new instance of {@link Query} with conditions derived from the given query.
     */
    static <T> Query<T> get(Query<T> query) {
        return SpecificationQuery.get(query);
    }

    /**
     * Creates a new {@link Query} instance with a single equality condition.
     * <br><br>
     * The specified attribute can include a nested path, such as <code>user.role.roleName</code>.
     * If the required join is not explicitly specified using the {@link #join(String, JoinType)} method,
     * it will be applied automatically as a {@link JoinType#LEFT}.
     *
     * @param attribute The attribute to filter on.
     * @param value     The value to compare against.
     * @param <T>       The type of the entity being queried.
     * @return A new instance of {@link Query} with the specified condition.
     */
    static <T> Query<T> where(String attribute, Object value) {
        return where(attribute, SearchOperator.EQ, value);
    }

    /**
     * Creates a new {@link Query} instance with a specified operator and value.
     * <br><br>
     * The specified attribute can include a nested path, such as <code>user.role.roleName</code>.
     * If the required join is not explicitly specified using the {@link #join(String, JoinType)} method,
     * it will be applied automatically as a {@link JoinType#LEFT}.
     *
     * @param attribute      The attribute to filter on.
     * @param searchOperator The {@link SearchOperator} to apply (e.g., EQ, LIKE, etc.).
     * @param value          The value to compare against.
     * @param <T>            The type of the entity being queried.
     * @return A new instance of {@link Query} with the specified condition.
     */
    static <T> Query<T> where(String attribute, SearchOperator searchOperator, Object value) {
        return SpecificationQuery.where(attribute, searchOperator, value);
    }

    /**
     * Adds an AND condition with an equality operator.
     * <br><br>
     * The specified attribute can include a nested path, such as <code>user.role.roleName</code>.
     * If the required join is not explicitly specified using the {@link #join(String, JoinType)} method,
     * it will be applied automatically as a {@link JoinType#LEFT}.
     *
     * @param attribute The attribute to filter on.
     * @param value     The value to compare against.
     * @return The current {@link Query} instance with the added condition.
     */
    Query<T> and(String attribute, Object value);

    /**
     * Adds an AND condition with a specified operator.
     * <br><br>
     * The specified attribute can include a nested path, such as <code>user.role.roleName</code>.
     * If the required join is not explicitly specified using the {@link #join(String, JoinType)} method,
     * it will be applied automatically as a {@link JoinType#LEFT}.
     *
     * @param attribute The attribute to filter on.
     * @param operator  The {@link SearchOperator} to apply (e.g., EQ, LIKE, etc.).
     * @param value     The value to compare against.
     * @return The current {@link Query} instance with the added condition.
     */
    Query<T> and(String attribute, SearchOperator operator, Object value);

    /**
     * Combines the current query with another query using an AND operator.
     *
     * @param query The query to combine with.
     * @return The current {@link Query} instance with the combined conditions.
     */
    Query<T> and(Query<T> query);

    /**
     * Adds an OR condition with an equality operator.
     * <br><br>
     * The specified attribute can include a nested path, such as <code>user.role.roleName</code>.
     * If the required join is not explicitly specified using the {@link #join(String, JoinType)} method,
     * it will be applied automatically as a {@link JoinType#LEFT}.
     *
     * @param attribute The attribute to filter on.
     * @param value     The value to compare against.
     * @return The current {@link Query} instance with the added condition.
     */
    Query<T> or(String attribute, Object value);

    /**
     * Adds an OR condition with a specified operator.
     * <br><br>
     * The specified attribute can include a nested path, such as <code>user.role.roleName</code>.
     * If the required join is not explicitly specified using the {@link #join(String, JoinType)} method,
     * it will be applied automatically as a {@link JoinType#LEFT}.
     *
     * @param attribute The attribute to filter on.
     * @param operator  The {@link SearchOperator} to apply (e.g., EQ, LIKE, etc.).
     * @param value     The value to compare against.
     * @return The current {@link Query} instance with the added condition.
     */
    Query<T> or(String attribute, SearchOperator operator, Object value);

    /**
     * Combines the current query with another query using an OR operator.
     *
     * @param query The query to combine with.
     * @return The current {@link Query} instance with the combined conditions.
     */
    Query<T> or(Query<T> query);

    /**
     * Adds a join condition to the query.
     *
     * @param joinAttribute The attribute to join on, supporting nested attributes.
     * @param joinType      The type of join to perform (e.g., INNER, LEFT, etc.).
     * @return The current {@link Query} instance with the added join condition.
     */
    SpecificationQuery<T> join(String joinAttribute, JoinType joinType);

    /**
     * Marks the query as distinct, ensuring only unique results are returned.
     *
     * @return The current {@link Query} instance marked as distinct.
     */
    Query<T> distinct();

    /**
     * Builds the {@link Specification} that represents all conditions, joins, and distinct settings
     * applied to this query.
     *
     * <p>This method collects all predicates from the built-up specification and applies distinct selection,
     * if specified. The resulting {@link Specification} can then be used to execute a query.</p>
     *
     * @return the combined {@link Specification} representing the current query conditions.
     */
    Specification<T> buildSpecification();
}

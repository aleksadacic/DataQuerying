package com.aleksadacic.springdataquerying.api;

import com.aleksadacic.springdataquerying.internal.specification.SpecificationQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Query interface for building <code>org.springframework.data.jpa.domain.Specification</code> objects
 * with various conditions and joins. This interface provides static factory methods for creating
 * query instances and methods for adding conditions dynamically.
 * <br>
 * This interface is also available as a <b>Spring Bean</b> with a default implementation,
 * so you can use dependency injection to get the instance.
 *
 * @param <T> The type of the entity being queried.
 */
public interface Query<T> {
    /**
     * Creates a new instance of a default {@link Query} implementation without any filters.
     *
     * @param <T> The type of the entity being queried.
     * @return A new instance of {@link Query}.
     */
    static <T> Query<T> get() {
        return SpecificationQuery.get();
    }

    /**
     * Creates a new {@link Query} instance based on an existing query.
     *
     * @param query The existing query to base the new query on.
     * @param <T>   The type of the entity being queried.
     * @return A new instance of {@link Query} with conditions derived from the given query.
     */
    static <T> Query<T> where(Query<T> query) {
        return SpecificationQuery.where(query);
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
     * Executes the query with all the applied conditions using the given {@link EntityManager}.
     *
     * @param entityManager The {@link EntityManager} to execute the query.
     * @param entityClass   The type of the entity being queried.
     * @param pojo          The class of the POJO to map the results to.
     * @param <R>           The type of the result (POJO).
     * @return A list of results mapped to the specified DTO class.
     */
    <R> List<R> executeQuery(EntityManager entityManager, Class<T> entityClass, Class<R> pojo);

    /**
     * Executes the query with all the applied conditions using the given {@link EntityManager} and returns a {@link Page}.
     *
     * @param entityManager The {@link EntityManager} to execute the query.
     * @param entityClass   The type of the entity being queried.
     * @param pojo          The class of the POJO to map the results to.
     * @param <R>           The type of the result (POJO).
     * @return A page of results mapped to the specified DTO class.
     */
    <R> Page<R> executeQuery(EntityManager entityManager, Class<T> entityClass, Class<R> pojo, PageRequest pageRequest);

    /**
     * Builds the {@link Specification} for the query, representing all the applied conditions.
     *
     * @return The {@link Specification} representing the current query conditions.
     */
    Specification<T> buildSpecification();

}

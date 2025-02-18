package dev.rosemarylab.dataquerying.internal.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * Utility class for executing projection queries.
 *
 * <p>This class provides methods to execute both non-paginated and paginated queries,
 * mapping the results to the desired projection (DTO) type using criteria queries and reflection.</p>
 */
class ProjectionQueryExecutor {
    private ProjectionQueryExecutor() {
    }

    /**
     * Executes a query that retrieves all entities matching the specified criteria,
     * maps the results to the projection type, and returns them as a list.
     *
     * <p>This method prepares the criteria query, executes it, maps the resulting tuples to field values,
     * and converts them to DTOs using reflection.</p>
     *
     * @param entityManager the entity manager used to create and execute the query
     * @param entityClass   the entity class to be queried
     * @param returnType    the projection (DTO) type to which results will be mapped
     * @param specification the specification defining the query predicate; may be {@code null}
     * @param distinct      {@code true} to eliminate duplicate results, {@code false} otherwise
     * @param <T>           the entity type
     * @param <P>           the projection type
     * @return a list of projections matching the criteria
     */
    public static <T, P> List<P> all(EntityManager entityManager, Class<T> entityClass, Class<P> returnType, Specification<T> specification, boolean distinct) {
        Map.Entry<CriteriaQuery<Tuple>, Root<T>> preparedQueryObjects =
                CriteriaQueryUtils.prepareCriteriaQuery(entityManager, entityClass, returnType, distinct, specification);
        CriteriaQuery<Tuple> criteriaQuery = preparedQueryObjects.getKey();

        // Execute the query
        TypedQuery<Tuple> query = entityManager.createQuery(criteriaQuery);
        List<Tuple> results = query.getResultList();

        List<Map<String, Object>> mappedResults = ProjectionUtils.mapTuplesToFieldValues(results, returnType);

        // Map the results to DTOs using reflection
        ObjectMapper mapper = new ObjectMapper();
        return ProjectionUtils.convertToDtoList(returnType, mappedResults, mapper);
    }

    /**
     * Executes a paginated query that retrieves entities matching the specified criteria,
     * maps the results to the projection type, and returns a {@code Page} of projections.
     *
     * <p>This method prepares the criteria query, applies sorting and pagination,
     * executes the query, counts the total number of matching entities,
     * and converts the results to DTOs using reflection.</p>
     *
     * @param entityManager the entity manager used to create and execute the query
     * @param entityClass   the entity class to be queried
     * @param returnType    the projection (DTO) type to which results will be mapped
     * @param specification the specification defining the query predicate; may be {@code null}
     * @param pageable      the pagination information
     * @param distinct      {@code true} to eliminate duplicate results, {@code false} otherwise
     * @param <T>           the entity type
     * @param <P>           the projection type
     * @return a page of projections matching the criteria
     */
    public static <T, P> Page<P> paged(EntityManager entityManager, Class<T> entityClass, Class<P> returnType, Specification<T> specification, Pageable pageable, boolean distinct) {
        Map.Entry<CriteriaQuery<Tuple>, Root<T>> preparedQueryObjects = CriteriaQueryUtils.prepareCriteriaQuery(entityManager, entityClass, returnType, distinct, specification);
        CriteriaQuery<Tuple> criteriaQuery = preparedQueryObjects.getKey();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        Root<T> root = preparedQueryObjects.getValue();

        // Apply sorting with support for joined paths
        CriteriaQueryUtils.applySorting(pageable, root, criteriaBuilder, criteriaQuery);

        // Execute the query with pagination
        TypedQuery<Tuple> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Tuple> results = query.getResultList();

        // Count total elements for pagination metadata
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        if (specification != null) {
            Predicate predicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
            if (predicate != null) {
                countQuery.where(predicate);
            }
        }
        countQuery.select(criteriaBuilder.count(countRoot));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        // Map the tuples to a list of maps with field values
        List<Map<String, Object>> mappedResults = ProjectionUtils.mapTuplesToFieldValues(results, returnType);

        // Map the results to DTOs using reflection
        ObjectMapper mapper = new ObjectMapper();
        List<P> content = ProjectionUtils.convertToDtoList(returnType, mappedResults, mapper);

        // Return a Page containing the content and pagination metadata
        return new PageImpl<>(content, pageable, totalElements);
    }
}

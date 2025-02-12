package io.github.aleksadacic.dataquerying.api;

import io.github.aleksadacic.dataquerying.internal.executor.GenericProjector;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Represents a projection operation that retrieves data from an underlying entity
 * and maps it to a different type (typically a DTO).
 *
 * @param <T> the entity type from which data is queried
 * @param <P> the projection (DTO) type to which the query results are mapped
 */
@SuppressWarnings("unused")
public interface Projection<T, P> {

    /**
     * Creates a new {@code Projection} instance backed by the given entity manager.
     *
     * @param entityManager the entity manager used to perform queries
     * @param type          the entity class to be queried
     * @param projection    the projection (DTO) class to which the results will be mapped
     * @param <T>           the entity type
     * @param <P>           the projection type
     * @return a new instance of {@code Projection} for the specified types
     */
    static <T, P> Projection<T, P> create(EntityManager entityManager, Class<T> type, Class<P> projection) {
        return new GenericProjector<>(entityManager, type, projection);
    }

    /**
     * Retrieves all entities projected to the projection type.
     *
     * @return a list of projections
     */
    List<P> findAll();

    /**
     * Retrieves all entities
     * and orders them according to the provided sort,
     * projected to the projection type.
     *
     * @param sort the {@link Sort} criteria
     * @return a list of sorted projections
     */
    List<P> findAll(Sort sort);

    /**
     * Retrieves all entities that satisfy the given specification,
     * projected to the projection type.
     *
     * @param specification the {@link Specification} defining the query
     * @return a list of projections matching the specification
     */
    List<P> findAll(Specification<T> specification);

    /**
     * Retrieves all entities that satisfy the given specification,
     * projected to the projection type. Allows for eliminating duplicate results using {@code distinct} property.
     *
     * @param specification the {@link Specification} defining the query predicate; may be {@code null}
     * @param distinct      {@code true} to eliminate duplicates, {@code false} otherwise
     * @return a list of projections matching the specification
     */
    List<P> findAll(Specification<T> specification, boolean distinct);

    /**
     * Retrieves all entities that satisfy the given specification
     * and orders them according to the provided sort,
     * projected to the projection type.
     *
     * @param specification the {@link Specification} defining the query predicate; may be {@code null}
     * @param sort          the {@link Sort} criteria
     * @return a list of sorted projections matching the specification
     */
    List<P> findAll(Specification<T> specification, Sort sort);

    /**
     * Retrieves all entities that satisfy the given specification, orders them according to the provided sort,
     * and optionally eliminates duplicates, projected to the projection type.
     *
     * @param specification the specification defining the query predicate; may be {@code null}
     * @param sort          the sort criteria
     * @param distinct      {@code true} to eliminate duplicates, {@code false} otherwise
     * @return a list of sorted projections matching the specification
     */
    List<P> findAll(Specification<T> specification, Sort sort, boolean distinct);

    /**
     * Retrieves a paginated list of entities
     * projected to the projection type.
     *
     * @param pageable the {@link Pageable} object which contains pagination information
     * @return a page of projections matching the specification
     */
    Page<P> findAll(Pageable pageable);

    /**
     * Retrieves a paginated list of entities that satisfy the given specification,
     * projected to the projection type.
     *
     * @param specification the {@link Specification} defining the query predicate; may be {@code null}
     * @param pageable      the {@link Pageable} object which contains pagination information
     * @return a page of projections matching the specification
     */
    Page<P> findAll(Specification<T> specification, Pageable pageable);

    /**
     * Retrieves a paginated list of entities that satisfy the given specification,
     * projected to the projection type, with an option to eliminate duplicates.
     *
     * @param specification the specification defining the query predicate; may be {@code null}
     * @param pageable      the {@link Pageable} object which contains pagination information
     * @param distinct      {@code true} to eliminate duplicates, {@code false} otherwise
     * @return a page of projections matching the specification
     */
    Page<P> findAll(Specification<T> specification, Pageable pageable, boolean distinct);

    /**
     * Retrieves all entities matching the criteria built by the provided query,
     * projected to the projection type.
     *
     * @param query the {@link Query} object which contains the data to build the {@link Specification} object
     * @return a list of projections matching the query criteria
     */
    List<P> findAll(Query<T> query);

    /**
     * Retrieves all entities matching the criteria built by the provided query and orders them using the provided sort,
     * projected to the projection type.
     *
     * @param query the {@link Query} object which contains the data to build the {@link Specification} object
     * @param sort  the sort criteria
     * @return a list of sorted projections matching the query criteria
     */
    List<P> findAll(Query<T> query, Sort sort);

    /**
     * Retrieves all entities matching the criteria built by the provided query, orders them using the provided sort,
     * and optionally eliminates duplicates, projected to the projection type.
     *
     * @param query    the {@link Query} object which contains the data to build the {@link Specification} object
     * @param sort     the sort criteria
     * @param distinct {@code true} to eliminate duplicates, {@code false} otherwise
     * @return a list of sorted projections matching the query criteria
     */
    List<P> findAll(Query<T> query, Sort sort, boolean distinct);

    /**
     * Retrieves a paginated list of entities matching the criteria built by the provided query,
     * projected to the projection type.
     *
     * @param query    the {@link Query} object which contains the data to build the {@link Specification} object
     * @param pageable the {@link Pageable} object which contains pagination information
     * @return a page of projections matching the query criteria
     */
    Page<P> findAll(Query<T> query, Pageable pageable);

    /**
     * Retrieves a paginated list of entities matching the criteria built by the provided query,
     * projected to the projection type, with an option to eliminate duplicates.
     *
     * @param query    the {@link Query} object which contains the data to build the {@link Specification} object
     * @param pageable the {@link Pageable} object which contains pagination information
     * @param distinct {@code true} to eliminate duplicates, {@code false} otherwise
     * @return a page of projections matching the query criteria
     */
    Page<P> findAll(Query<T> query, Pageable pageable, boolean distinct);
}


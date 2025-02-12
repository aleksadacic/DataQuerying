package io.github.aleksadacic.dataquerying.internal.executor;

import io.github.aleksadacic.dataquerying.api.Projection;
import io.github.aleksadacic.dataquerying.api.Query;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Default implementation of the {@link Projection} interface.
 *
 * <p>This class uses an {@link EntityManager} to perform queries and maps the results to the projection
 * (DTO) type using the {@link ProjectionQueryExecutor}.</p>
 *
 * @param <T> the entity type from which data is queried
 * @param <P> the projection (DTO) type to which results are mapped
 */
public class GenericProjector<T, P> implements Projection<T, P> {
    private final EntityManager entityManager;
    private final Class<T> type;
    private final Class<P> projection;

    /**
     * Constructs a new {@code GenericProjector}.
     *
     * @param entityManager the entity manager used to perform queries
     * @param type          the entity class to be queried
     * @param projection    the projection (DTO) class to which results will be mapped
     */
    public GenericProjector(EntityManager entityManager, Class<T> type, Class<P> projection) {
        this.entityManager = entityManager;
        this.type = type;
        this.projection = projection;
    }

    @Override
    public List<P> findAll() {
        return findAll((Specification<T>) null, (Sort) null, false);
    }

    @Override
    public List<P> findAll(Sort sort) {
        return findAll((Specification<T>) null, sort, false);
    }

    @Override
    public List<P> findAll(Specification<T> specification) {
        return findAll(specification, (Sort) null, false);
    }

    @Override
    public List<P> findAll(Specification<T> specification, boolean distinct) {
        return findAll(specification, (Sort) null, distinct);
    }

    @Override
    public List<P> findAll(Specification<T> specification, Sort sort) {
        return findAll(specification, sort, false);
    }

    @Override
    public List<P> findAll(Specification<T> specification, Sort sort, boolean distinct) {
        return ProjectionQueryExecutor.all(entityManager, type, projection, specification, distinct);
    }

    @Override
    public Page<P> findAll(Pageable pageable) {
        return findAll((Specification<T>) null, pageable, false);
    }

    @Override
    public Page<P> findAll(Specification<T> specification, Pageable pageable) {
        return findAll(specification, pageable, false);
    }

    @Override
    public Page<P> findAll(Specification<T> specification, Pageable pageable, boolean distinct) {
        return ProjectionQueryExecutor.paged(entityManager, type, projection, specification, pageable, false);
    }

    @Override
    public List<P> findAll(Query<T> query) {
        return findAll(query, (Sort) null, false);
    }

    @Override
    public List<P> findAll(Query<T> query, Sort sort) {
        return findAll(query, sort, false);
    }

    @Override
    public List<P> findAll(Query<T> query, Sort sort, boolean distinct) {
        return findAll(query.buildSpecification(), sort, distinct);
    }

    @Override
    public Page<P> findAll(Query<T> query, Pageable pageable) {
        return findAll(query, pageable, false);
    }

    @Override
    public Page<P> findAll(Query<T> query, Pageable pageable, boolean distinct) {
        if (query == null)
            throw new IllegalArgumentException("Query cannot be null.");
        return findAll(query.buildSpecification(), pageable, distinct);
    }
}

package dev.rosemarylab.dataquerying.api;

import jakarta.persistence.EntityManager;

public class ProjectionFactory {
    private final EntityManager entityManager;

    public ProjectionFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T, P> Projection<T, P> create(Class<T> type, Class<P> projection) {
        return Projection.create(entityManager, type, projection);
    }
}

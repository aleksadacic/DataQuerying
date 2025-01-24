package com.aleksadacic.springdataquerying.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryTestBase {
    protected EntityManager entityManager;
    protected CriteriaBuilder criteriaBuilder;
    protected CriteriaQuery<Object> criteriaQuery;
    protected Root root;
    protected TypedQuery<Object> typedQuery;

    @BeforeEach
    @SuppressWarnings("unchecked")
    protected void setUp() {
        // Arrange
        entityManager = mock(EntityManager.class);
        criteriaBuilder = mock(CriteriaBuilder.class);
        criteriaQuery = mock(CriteriaQuery.class);
        root = mock(Root.class);
        typedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Object.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Object.class)).thenReturn(root);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
    }

    @SuppressWarnings("unchecked")
    protected void buildSpecificationMock(Boolean distinct, Predicate predicate) {
        CriteriaQuery<Object> criteriaQueryWithPredicates = mock(CriteriaQuery.class);
        Predicate combinedPredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        // Handle distinct logic
        if (distinct != null) {
            when(criteriaQuery.distinct(distinct)).thenReturn(criteriaQuery);
            when(criteriaQuery.isDistinct()).thenReturn(distinct); // Mock the isDistinct method
        }

        when(criteriaBuilder.and(predicate)).thenReturn(combinedPredicate); //TODO ovo prebaci u test deo

        when(criteriaQuery.where(combinedPredicate)).thenReturn(criteriaQueryWithPredicates);
        when(criteriaQueryWithPredicates.getRestriction()).thenReturn(finalPredicate);
    }
}

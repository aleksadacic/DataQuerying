package com.aleksadacic.springdataquerying.query;

import com.aleksadacic.springdataquerying.api.Query;
import com.aleksadacic.springdataquerying.api.SearchOperator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//TODO
class QueryTest {

    private EntityManager entityManager;
    private CriteriaBuilder criteriaBuilder;
    private CriteriaQuery<Object[]> criteriaQuery;
    private Root<Object> root;
    private TypedQuery<Object[]> typedQuery;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        criteriaBuilder = mock(CriteriaBuilder.class);
        criteriaQuery = mock(CriteriaQuery.class);
        root = mock(Root.class);
        typedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Object[].class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Object.class)).thenReturn(root);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
    }

    @Test
    void testWhereWithAttributeAndValue() {
        // Arrange
        Query<Object> query = Query.where("name", "John");

        // Manually create a Specification for comparison
        Specification<Object> expectedSpecification = (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("name"), "John");

        // Act
        Specification<Object> actualSpecification = query.buildSpecification();

        // Assert
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<Object> criteriaQuery = mock(CriteriaQuery.class);
        Root<Object> root = mock(Root.class);

        Predicate expectedPredicate = expectedSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
        Predicate actualPredicate = actualSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertEquals(expectedPredicate, actualPredicate, "The generated specification should match the manually created one");
    }

    @Test
    void testWhereWithOperator() {
        // Arrange
        Query<Object> query = Query.where("age", SearchOperator.GT, 25);

        Path<Object> agePath = mock(Path.class);
        when(root.get("age")).thenReturn(agePath);

        // Manually create a Specification for comparison
        Specification<Object> expectedSpecification = (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("age"), 25);

        // Act
        Specification<Object> actualSpecification = query.buildSpecification();

        // Assert
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<Object> criteriaQuery = mock(CriteriaQuery.class);
        Root<Object> root = mock(Root.class);

        Predicate expectedPredicate = expectedSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
        Predicate actualPredicate = actualSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertNotNull(expectedPredicate);
        assertNotNull(actualPredicate);
        assertEquals(expectedPredicate, actualPredicate, "The generated specification should match the manually created one");
    }

    @Test
    void testDistinct() {
        Query<Object> query = Query.get();
        query.distinct();

        assertTrue(query.buildSpecification().toPredicate(root, criteriaQuery, criteriaBuilder) != null, "Distinct should modify the query");
    }
}

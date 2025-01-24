package com.aleksadacic.springdataquerying.query;

import com.aleksadacic.springdataquerying.api.Query;
import com.aleksadacic.springdataquerying.api.SearchOperator;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChainOperationsTest {

    @Test
    @SuppressWarnings("unchecked")
    void testWithJoin() {
        // Arrange
        Root<Object> root = mock(Root.class);
        CriteriaQuery<Object> criteriaQuery = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        // Mock
        Join<Object, Object> ordersJoin = mock(Join.class);
        Join<Object, Object> paymentJoin = mock(Join.class);
        Path<Object> agePath = mock(Path.class);

        // Mock the behavior of root join
        Set<Join<Object, ?>> joins = new HashSet<>();
        when(root.join("orders", JoinType.INNER)).then(invocation -> {
            joins.add(ordersJoin);
            return ordersJoin;
        });
        when(ordersJoin.join("payment", JoinType.INNER)).then(invocation -> {
            joins.add(paymentJoin);
            return paymentJoin;
        });
        when(root.getJoins()).thenReturn(joins);

        // Mock the behavior of root to path
        when(root.get("age")).thenReturn(agePath);

        // Mock interactions for CriteriaBuilder
        Predicate agePredicate = mock(Predicate.class);

        // Mock behavior for CriteriaBuilder
        when(agePath.as(Integer.class)).thenReturn(mock(Path.class));
        when(criteriaBuilder.greaterThan(any(Path.class), eq(25))).thenReturn(agePredicate);
        when(criteriaBuilder.and(agePredicate)).thenReturn(agePredicate);

        // Additional mocks for CriteriaQuery
        when(criteriaQuery.where(agePredicate)).thenReturn(criteriaQuery);
        when(criteriaQuery.getRestriction()).thenReturn(agePredicate);

        Query<Object> query = Query.get();
        query.join("orders.payment", JoinType.INNER).and("age", SearchOperator.GT, 25);

        // Act
        Specification<Object> actualSpecification = query.buildSpecification();
        Predicate actualPredicate = actualSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        // Assert
        assertNotNull(actualPredicate, "The predicate should not be null");
        assertEquals(2, root.getJoins().size(), "The root should have two joins");
        assertTrue(root.getJoins().contains(ordersJoin), "The root should contain the 'orders' join");
        assertTrue(root.getJoins().contains(paymentJoin), "The root should contain the 'payment' join");
        verify(root).join("orders", JoinType.INNER);
        verify(ordersJoin).join("payment", JoinType.INNER);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testWithAndConditions() {
        // Arrange
        Root<Object> root = mock(Root.class);
        CriteriaQuery<Object> criteriaQuery = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        // Mock Path objects for name and age
        Path<Object> namePath = mock(Path.class);
        Path<Object> agePath = mock(Path.class);

        // Mock interactions for root
        when(root.get("name")).thenReturn(namePath);
        when(root.get("age")).thenReturn(agePath);

        // Mock interactions for CriteriaBuilder
        Predicate namePredicate = mock(Predicate.class);
        Predicate agePredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        // Mock behavior for CriteriaBuilder
        when(criteriaBuilder.equal(namePath, "John")).thenReturn(namePredicate);
        when(agePath.as(Integer.class)).thenReturn(mock(Path.class));
        when(criteriaBuilder.greaterThan(any(Path.class), eq(25))).thenReturn(agePredicate);
        when(criteriaBuilder.and(namePredicate, agePredicate)).thenReturn(combinedPredicate);

        // Additional mocks for CriteriaQuery
        when(criteriaQuery.where(combinedPredicate)).thenReturn(criteriaQuery);
        when(criteriaQuery.getRestriction()).thenReturn(combinedPredicate);

        Query<Object> query = Query.get()
                .and("name", "John")
                .and("age", SearchOperator.GT, 25);

        // Act
        Specification<Object> specification = query.buildSpecification();
        Predicate result = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        // Assert
        assertNotNull(result, "Predicate should not be null");
        assertEquals(combinedPredicate, result, "The combined predicate should match the expected predicate");

        // Verify interactions
        verify(root).get("name");
        verify(root).get("age");
        verify(criteriaBuilder).equal(namePath, "John");
        verify(criteriaBuilder).greaterThan(any(Path.class), eq(25));
        verify(criteriaBuilder).and(namePredicate, agePredicate);
    }
}

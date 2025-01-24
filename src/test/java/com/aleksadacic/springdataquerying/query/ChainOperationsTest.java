package com.aleksadacic.springdataquerying.query;

import com.aleksadacic.springdataquerying.api.Query;
import com.aleksadacic.springdataquerying.api.SearchOperator;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChainOperationsTest extends QueryTestBase {

    @Test
    @SuppressWarnings("unchecked")
    void testWithJoin() {
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

        buildSpecificationMock(null, agePredicate);

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
        // Mock the behavior of root to path
        Path<Integer> agePath = mock(Path.class);
        Path<String> namePath = mock(Path.class);
        when(root.get("age")).thenReturn(agePath);
        when(root.get("name")).thenReturn(namePath);

        // Mock predicates for age and name
        Predicate agePredicate = mock(Predicate.class);
        Predicate namePredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        // Mock CriteriaBuilder behavior
        when(agePath.as(Integer.class)).thenReturn(agePath);
        when(namePath.as(String.class)).thenReturn(namePath);
        when(criteriaBuilder.greaterThan(agePath, 25)).thenReturn(agePredicate);
        when(criteriaBuilder.equal(namePath, "John")).thenReturn(namePredicate);
        when(criteriaBuilder.and(agePredicate, namePredicate)).thenReturn(combinedPredicate);

        buildSpecificationMock(null, combinedPredicate);

        // Act
        Query<Object> query = Query
                .where("age", SearchOperator.GT, 25)
                .and("name", SearchOperator.EQ, "John");
        Specification<Object> actualSpecification = query.buildSpecification();
        Predicate actualPredicate = actualSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        // Assert
        assertNotNull(actualPredicate, "The predicate should not be null");
        assertEquals(combinedPredicate, actualPredicate, "The combined predicate should match the expected predicate");

        // Verify interactions
        verify(root).get("age");
        verify(root).get("name");
        verify(criteriaBuilder).greaterThan(agePath, 25);
        verify(criteriaBuilder).equal(namePath, "John");
        verify(criteriaBuilder).and(agePredicate, namePredicate);
    }

}

package com.aleksadacic.springdataquerying.query;

import com.aleksadacic.springdataquerying.api.Query;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JoinTest {

    @Test
    @SuppressWarnings("unchecked")
    void testSimpleJoin() {
        // Mock
        Join<Object, Object> ordersJoin = mock(Join.class);

        // Arrange
        Root<Object> root = mock(Root.class);
        CriteriaQuery<Object> criteriaQuery = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        // Mock the behavior of root.getJoins()
        Set<Join<Object, ?>> joins = new HashSet<>();
        when(root.join("orders", JoinType.INNER)).then(invocation -> {
            joins.add(ordersJoin);
            return ordersJoin;
        });
        when(root.getJoins()).thenReturn(joins);

        Query<Object> query = Query.get();
        query.join("orders", JoinType.INNER);

        // Act
        Specification<Object> actualSpecification = query.buildSpecification();
        Predicate actualPredicate = actualSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        // Assert
        assertNull(actualPredicate, "The predicate for a join-only query should be null");
        assertEquals(1, root.getJoins().size(), "The root should have one join");
        assertTrue(root.getJoins().contains(ordersJoin), "The root should contain the 'orders' join");
        verify(root).join("orders", JoinType.INNER);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNestedJoin() {
        // Mock
        Join<Object, Object> ordersJoin = mock(Join.class);
        Join<Object, Object> paymentJoin = mock(Join.class);

        // Arrange
        Root<Object> root = mock(Root.class);
        CriteriaQuery<Object> criteriaQuery = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        // Mock the behavior of root.getJoins()
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

        Query<Object> query = Query.get();
        query.join("orders.payment", JoinType.INNER);

        // Act
        Specification<Object> actualSpecification = query.buildSpecification();
        Predicate actualPredicate = actualSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        // Assert
        assertNull(actualPredicate, "The predicate for a join-only query should be null");
        assertEquals(2, root.getJoins().size(), "The root should have two joins");
        assertTrue(root.getJoins().contains(ordersJoin), "The root should contain the 'orders' join");
        assertTrue(root.getJoins().contains(paymentJoin), "The root should contain the 'payment' join");
        verify(root).join("orders", JoinType.INNER);
        verify(ordersJoin).join("payment", JoinType.INNER);
    }
}

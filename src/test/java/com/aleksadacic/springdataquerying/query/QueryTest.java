package com.aleksadacic.springdataquerying.query;

import com.aleksadacic.springdataquerying.api.Query;
import com.aleksadacic.springdataquerying.api.SearchOperator;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class QueryTest extends QueryTestBase {
    @Test
    @SuppressWarnings("unchecked")
    void testWhereWithOperator() {
        // Mock the behavior of root to path
        Path<Object> agePath = mock(Path.class);
        when(root.get("age")).thenReturn(agePath);

        // Mock predicate for age
        Predicate agePredicate = mock(Predicate.class);

        // Mock behaviour for CriteriaBuilder // specification wrapper internal
        when(agePath.as(Integer.class)).thenReturn(mock(Path.class));
        when(criteriaBuilder.greaterThan(any(Path.class), eq(25))).thenReturn(agePredicate);

        buildSpecificationMock(null, agePredicate);

        // Act
        Query<Object> query = Query.where("age", SearchOperator.GT, 25);
        Specification<Object> actualSpecification = query.buildSpecification();
        Predicate actualPredicate = actualSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        // Assert
        assertNotNull(actualPredicate, "The predicate should not be null");

        // Verify interactions
        verify(root).get("age");
        verify(criteriaBuilder).greaterThan(any(Path.class), eq(25));
    }

    @Test
    void testDistinct() {
        buildSpecificationMock(true);

        Query<Object> query = Query.get().distinct();
        Specification<Object> specification = query.buildSpecification();
        Predicate actualPredicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertNull(actualPredicate, "Predicate should be null");
        assertTrue(criteriaQuery.isDistinct(), "CriteriaQuery should be distinct");

        verify(criteriaQuery).distinct(true); // Verify that distinct(true) was called
    }
}

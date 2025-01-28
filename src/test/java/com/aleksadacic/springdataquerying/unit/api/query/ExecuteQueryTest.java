package com.aleksadacic.springdataquerying.unit.api.query;

import com.aleksadacic.springdataquerying.api.Query;
import com.aleksadacic.springdataquerying.api.SearchOperator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import utils.Dto;
import utils.DtoMinimal;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class ExecuteQueryTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<Tuple> criteriaQuery;
    @Mock
    private Root<Dto> root;
    @Mock
    private TypedQuery<Tuple> typedQuery;
    @Mock
    private Tuple tuple;

    // Paths for age and name
    @Mock
    private Path agePath;
    @Mock
    private Path namePath;

    @Mock
    private CriteriaQuery<Long> countCriteriaQuery;

    @Mock
    private Root<Dto> countRoot;

    @Mock
    private TypedQuery<Long> countTypedQuery;

    @Mock
    private Expression<Long> countExpression;

    @Captor
    private ArgumentCaptor<Predicate> singlePredicateCaptor;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createTupleQuery()).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Dto.class)).thenReturn(root);

        // Return the same criteriaQuery in builder chain style
        when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);

        // Use typedQuery for final fetch
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.singletonList(tuple));
        when(tuple.get("name")).thenReturn("John"); // Assuming DtoMinimal has a 'name' field
        when(tuple.get("age")).thenReturn("26"); // Assuming DtoMinimal has a 'age' field

        // Stub the path for "age"
        when(root.get("age")).thenReturn(agePath);
        when(criteriaBuilder.greaterThan(agePath, 20)).thenReturn(mock(Predicate.class));

        // Stub the path for "name" (for selection to avoid null)
        when(root.get("name")).thenReturn(namePath);
        when(namePath.as(String.class)).thenReturn(namePath); // helps with selection

        // Stub CriteriaBuilder and CriteriaQuery for count query
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(countCriteriaQuery);
        when(countCriteriaQuery.from(Dto.class)).thenReturn(countRoot);

        // Stub count query selection
        when(criteriaBuilder.count(countRoot)).thenReturn(countExpression);
        when(countCriteriaQuery.select(countExpression)).thenReturn(countCriteriaQuery);

        // Stub EntityManager to return TypedQuery<Long> for count query
        when(entityManager.createQuery(countCriteriaQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(1L);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testExecuteQuery_ageGreaterThan20() {
        // 1) Build the Query
        Query<Dto> query = Query.where("age", SearchOperator.GT, 20);

        // 2) Execute
        List<DtoMinimal> result = query.executeQuery(entityManager, Dto.class, DtoMinimal.class);

        // 3) Basic checks
        assertNotNull(result, "Result list should not be null");
        assertEquals(1, result.size(), "Should have exactly one row");
        assertEquals("John", result.getFirst().getName());

        // 4) Verify we set the single predicate in "where(...)"
        verify(criteriaQuery).where(singlePredicateCaptor.capture());
        Predicate usedPredicate = singlePredicateCaptor.getValue();
        assertNotNull(usedPredicate, "Predicate must not be null");

        // 5) We can also confirm the path usage
        verify(root).get("age");
        verify(criteriaBuilder).greaterThan(agePath, 20);

        // Optionally verify selection call if needed
        verify(criteriaQuery).select(any()); // The actual "select(...)" call
    }

    @Test
    void testExecuteQueryWithPageRequest_ageGreaterThan20() {
        // Build the Query with a PageRequest
        Query<Dto> query = Query.where("age", SearchOperator.GT, 20);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        // Stub CriteriaBuilder methods for sorting
        Order order = mock(Order.class);
        when(criteriaBuilder.asc(namePath)).thenReturn(order);
        when(criteriaBuilder.desc(namePath)).thenReturn(order); // Not used in this test

        // 4) Execute
        Page<DtoMinimal> pageResult = query.executeQuery(entityManager, Dto.class, DtoMinimal.class, pageRequest);

        assertNotNull(pageResult, "Page result should not be null");
        assertEquals(1, pageResult.getContent().size(), "Should have exactly one row in content");
        assertEquals("John", pageResult.getContent().getFirst().getName());

        // Verify that the main query was set up correctly
        verify(criteriaQuery).where(singlePredicateCaptor.capture());
        Predicate usedPredicate = singlePredicateCaptor.getValue();
        assertNotNull(usedPredicate, "Predicate must not be null");

        // Verify path usage and predicate creation
        verify(root).get("age");
        verify(criteriaBuilder).greaterThan(agePath, 20);

        // Verify sorting was applied
        verify(criteriaQuery).orderBy(List.of(order));

        // Verify selection was applied
        verify(criteriaQuery).select(any());

        // Verify TypedQuery was executed with pagination parameters
        verify(typedQuery).setFirstResult(0);
        verify(typedQuery).setMaxResults(10);
        verify(typedQuery).getResultList();

        // Verify count query was created and executed
        verify(criteriaBuilder).createQuery(Long.class);
        verify(countCriteriaQuery).from(Dto.class);
        verify(countCriteriaQuery).select(countExpression);
        verify(entityManager).createQuery(countCriteriaQuery);
        verify(countTypedQuery).getSingleResult();
    }
}

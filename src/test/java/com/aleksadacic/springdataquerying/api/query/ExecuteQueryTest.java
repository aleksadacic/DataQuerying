package com.aleksadacic.springdataquerying.api.query;

import com.aleksadacic.springdataquerying.api.Query;
import com.aleksadacic.springdataquerying.api.SearchOperator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import utils.Dto;
import utils.DtoMinimal;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class ExecuteQueryTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<DtoMinimal> criteriaQuery;
    @Mock
    private Root<Dto> root;
    @Mock
    private TypedQuery<DtoMinimal> typedQuery;

    // Paths for age and name
    @Mock
    private Path agePath;
    @Mock
    private Path namePath;

    @Captor
    private ArgumentCaptor<Predicate> singlePredicateCaptor;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(DtoMinimal.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Dto.class)).thenReturn(root);

        // Return the same criteriaQuery in builder chain style
        when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);

        // Use typedQuery for final fetch
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.singletonList(new DtoMinimal("John")));

        // Stub the path for "age"
        when(root.get("age")).thenReturn(agePath);
        when(criteriaBuilder.greaterThan(agePath, 20)).thenReturn(mock(Predicate.class));

        // Stub the path for "name" (for selection to avoid null)
        when(root.get("name")).thenReturn(namePath);
        when(namePath.as(String.class)).thenReturn(namePath); // helps with selection
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
}

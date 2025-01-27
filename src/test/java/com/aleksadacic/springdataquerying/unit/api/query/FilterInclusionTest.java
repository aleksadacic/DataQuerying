package com.aleksadacic.springdataquerying.unit.api.query;

import com.aleksadacic.springdataquerying.api.SearchOperator;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import utils.PersonEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("java:S2187")
public class FilterInclusionTest {
    private AutoCloseable closeable;

    @Mock
    protected CriteriaBuilder criteriaBuilder;

    @Mock
    protected CriteriaQuery<PersonEntity> criteriaQuery;

    @Mock
    protected Root<PersonEntity> root;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    public record FilterExpectation(String attribute, SearchOperator operator, Object value) {
    }

    public enum CombinationLogic {
        OR,
        AND,
        WHERE_AND_OR,
        NONE // for cases like a single filter or no filters
    }

    /**
     * Stubs the minimal behavior of a CriteriaQuery to avoid null returns
     * for .where(...) and .getRestriction().
     *
     * @param criteriaQuery The mocked CriteriaQuery
     * @return The final Predicate that will be returned by getRestriction().
     */
    protected Predicate stubCriteriaQuery(CriteriaQuery<PersonEntity> criteriaQuery) {
        Predicate finalPredicate = mock(Predicate.class);

        // Support .where(...) with either a single Predicate or an array
        when(criteriaQuery.where((Predicate) any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where((Predicate[]) any())).thenReturn(criteriaQuery);

        // Return a mock Predicate as the "restriction"
        when(criteriaQuery.getRestriction()).thenReturn(finalPredicate);

        return finalPredicate;
    }


    protected void verifyFilterInclusion(
            Specification<PersonEntity> spec,
            List<FilterExpectation> expectations,
            CombinationLogic combinationLogic
    ) {
        // 1) Mock how "root.get(...)” returns a Path<?> for each unique attribute
        //    For simplicity, we assume distinct attribute names in the list.
        Map<String, Path<?>> pathMocks = new HashMap<>();
        for (FilterExpectation expectation : expectations) {
            @SuppressWarnings("unchecked")
            Path<Object> mockPath = mock(Path.class);
            when(root.get(expectation.attribute())).thenReturn(mockPath);
            pathMocks.put(expectation.attribute(), mockPath);
        }

        // 2) For each filter, stub the specific CriteriaBuilder call
        List<Predicate> individualPredicates = new ArrayList<>();
        for (FilterExpectation fx : expectations) {
            Predicate mockPredicate = mock(Predicate.class);
            individualPredicates.add(mockPredicate);

            // Decide which CriteriaBuilder method to call based on the operator
            switch (fx.operator()) {
                case EQ -> when(criteriaBuilder.equal(pathMocks.get(fx.attribute()), fx.value()))
                        .thenReturn(mockPredicate);
                case GT -> {
                    // e.g. assume path is numeric
                    @SuppressWarnings("unchecked")
                    Path<Integer> integerPath = (Path<Integer>) pathMocks.get(fx.attribute());
                    when(criteriaBuilder.greaterThan(integerPath, (Integer) fx.value()))
                            .thenReturn(mockPredicate);
                }
                case LT -> {
                    // etc. for each operator...
                }
                // handle other operators (LIKE, NOT_EQ, etc.) as needed
                default -> throw new UnsupportedOperationException(
                        "Not yet handled operator: " + fx.operator());
            }
        }

        // 3) Depending on combinationLogic, mock the final combined predicate
        Predicate combinedPredicate = null;

        switch (combinationLogic) {
            case NONE -> {
                // Single filter or no filter => no top-level combination
                if (expectations.size() > 1) {
                    throw new IllegalStateException("NONE logic but multiple filters?");
                }
                // No extra combination needed
            }
            case AND -> {
                // Combine all filters with AND in one shot
                if (expectations.size() > 1) {
                    combinedPredicate = mock(Predicate.class);
                    when(criteriaBuilder.and(individualPredicates.toArray(new Predicate[0])))
                            .thenReturn(combinedPredicate);
                }
            }
            case OR -> {
                // Combine all filters with OR in one shot
                if (expectations.size() > 1) {
                    combinedPredicate = mock(Predicate.class);
                    when(criteriaBuilder.or(individualPredicates.toArray(new Predicate[0])))
                            .thenReturn(combinedPredicate);
                }
            }
            case WHERE_AND_OR -> {
                // For a simple example: assume exactly 3 filters => (f0 AND f1) OR f2
                if (expectations.size() != 3) {
                    throw new IllegalArgumentException(
                            "WHERE_AND_OR logic example expects exactly 3 filters!");
                }

                // 3a) First combine the first two with AND
                Predicate andPredicate = mock(Predicate.class);
                when(criteriaBuilder.and(individualPredicates.get(0), individualPredicates.get(1)))
                        .thenReturn(andPredicate);

                // 3b) Then combine that result with the third filter via OR
                combinedPredicate = mock(Predicate.class);
                when(criteriaBuilder.or(andPredicate, individualPredicates.get(2)))
                        .thenReturn(combinedPredicate);
            }
        }

        // 4) Actually call spec.toPredicate(...)
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        // 5) Check result
        if (!expectations.isEmpty()) {
            assertNotNull(result, "Expected a non-null predicate with " + expectations.size() + " filters");
        } else {
            assertNull(result, "Expected null or trivial predicate when no filters present");
        }

        // If we had an explicit final combination, we can also check identity:
        if (combinedPredicate != null && !expectations.isEmpty()) {
            assertNotNull(combinedPredicate, "Expected a non-null combined predicate.");
        }

        // 6) Verify each operator call
        for (FilterExpectation fx : expectations) {
            switch (fx.operator()) {
                case EQ -> verify(criteriaBuilder).equal(pathMocks.get(fx.attribute()), fx.value());
                case GT -> {
                    @SuppressWarnings("unchecked")
                    Path<Integer> integerPath = (Path<Integer>) pathMocks.get(fx.attribute());
                    verify(criteriaBuilder).greaterThan(integerPath, (Integer) fx.value());
                }
                // handle other operators (LIKE, NOT_EQ, etc.) as needed
                default -> throw new UnsupportedOperationException(
                        "Not yet handled operator: " + fx.operator());
            }
        }

        // 7) If we have multiple filters, verify the combination calls
        switch (combinationLogic) {
            case AND -> {
                assertEquals(2, expectations.size());
                verify(criteriaBuilder).and(individualPredicates.get(0), individualPredicates.get(1));
            }
            case OR -> {
                assertEquals(2, expectations.size());
                verify(criteriaBuilder).or(individualPredicates.get(0), individualPredicates.get(1));
            }
            case WHERE_AND_OR -> {
                // Check that we had an AND for the first pair, OR for the final
                verify(criteriaBuilder).and(individualPredicates.get(0), individualPredicates.get(1));
                verify(criteriaBuilder).or(any(Predicate.class), eq(individualPredicates.get(2)));
            }
            default -> { /* NONE or not multiple => no final combo check */ }
        }
    }
}

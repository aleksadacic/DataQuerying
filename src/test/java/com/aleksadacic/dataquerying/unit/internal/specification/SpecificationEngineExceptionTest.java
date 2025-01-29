package com.aleksadacic.dataquerying.unit.internal.specification;

import com.aleksadacic.dataquerying.api.SearchOperator;
import com.aleksadacic.dataquerying.api.exceptions.SpecificationBuilderException;
import com.aleksadacic.dataquerying.internal.specification.Filter;
import com.aleksadacic.dataquerying.internal.specification.SpecificationEngine;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("rawtypes")
class SpecificationEngineExceptionTest {
    private AutoCloseable closeable;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<?> fieldPath;

    @Mock
    private Expression<? extends Comparable> comparablePath;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testInOperator_notACollection_throwsException() {
        // Given a Filter with operator IN, but value is not a Collection
        Filter filter = new Filter("age", SearchOperator.IN, 123); // int instead of collection

        // When & Then
        assertThrows(SpecificationBuilderException.class, () ->
                        SpecificationEngine.in(filter, criteriaBuilder, fieldPath),
                "IN operator requires a collection of values"
        );
    }

    @Test
    void testBetweenOperator_notAList_throwsException() {
        // Given a Filter with operator BETWEEN, but value is not a List
        Filter filter = new Filter("price", SearchOperator.BETWEEN, 100); // single int

        // Should throw error: "BETWEEN operator requires a list of two comparable values"
        assertThrows(SpecificationBuilderException.class, () ->
                SpecificationEngine.between(filter, criteriaBuilder, comparablePath)
        );
    }

    @Test
    void testBetweenOperator_listWrongSize_throwsException() {
        // Value is a List, but not exactly 2 elements
        Filter filter = new Filter("price", SearchOperator.BETWEEN, List.of(10, 20, 30));

        // Should throw "BETWEEN operator requires a list of two comparable values"
        assertThrows(SpecificationBuilderException.class, () ->
                SpecificationEngine.between(filter, criteriaBuilder, comparablePath)
        );
    }

    @Test
    void testNotLikeOperator_nonString_throwsException() {
        // NOT_LIKE operator, but the value is not a String
        Filter filter = new Filter("name", SearchOperator.NOT_LIKE, 123); // an integer

        assertThrows(SpecificationBuilderException.class, () ->
                        SpecificationEngine.notLike(filter, criteriaBuilder, fieldPath),
                "NOT_LIKE operator requires a String value"
        );
    }

    @Test
    void testLikeOperator_nonString_throwsException() {
        // LIKE operator, value is not a String
        Filter filter = new Filter("description", SearchOperator.LIKE, true); // boolean

        assertThrows(SpecificationBuilderException.class, () ->
                        SpecificationEngine.like(filter, criteriaBuilder, fieldPath),
                "LIKE operator requires a String value"
        );
    }
}

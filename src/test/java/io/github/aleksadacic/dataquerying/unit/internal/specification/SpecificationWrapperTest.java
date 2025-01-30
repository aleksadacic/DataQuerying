package io.github.aleksadacic.dataquerying.unit.internal.specification;

import io.github.aleksadacic.dataquerying.api.SearchOperator;
import io.github.aleksadacic.dataquerying.api.exceptions.AttributeNotFoundException;
import io.github.aleksadacic.dataquerying.api.exceptions.JoinNotFoundException;
import io.github.aleksadacic.dataquerying.internal.specification.Filter;
import io.github.aleksadacic.dataquerying.internal.specification.SpecificationWrapper;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import io.github.aleksadacic.dataquerying.utils.Dto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class SpecificationWrapperTest {
    private AutoCloseable closeable;

    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<Object[]> criteriaQuery;
    @Mock
    private Root<Dto> root;
    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private Path stringPath;

    @Mock
    private Path numberPath;

    @Mock
    private Path comparablePath;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testToPredicateWithNullFilter() {
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(null);
        Predicate predicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertNull(predicate, "Predicate should be null if no filter is provided.");
    }

    @Test
    void testToPredicateWithInvalidFilter() {
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(new Filter("name", SearchOperator.GT, null));
        Predicate predicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertNull(predicate, "Predicate should be null if filter is invalid.");
    }

    @Test
    void testToPredicateEq() {
        // 1) Create Filter and Wrapper
        Filter filter = new Filter("age", SearchOperator.EQ, 30);
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        // 2) Mock root.get(...) -> a numeric Path
        when(root.get("age")).thenReturn(numberPath);

        // 3) Mock criteriaBuilder.equal(...)
        Predicate eqPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(numberPath, 30)).thenReturn(eqPredicate);

        // 4) Call toPredicate
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // 5) Verify
        assertSame(eqPredicate, result, "Should return the eqPredicate from criteriaBuilder");
        verify(criteriaBuilder).equal(numberPath, 30);
    }

    @Test
    void testToPredicateEq_withNullValue() {
        Filter filter = new Filter("age", SearchOperator.EQ, null);
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("age")).thenReturn(numberPath);

        // If value is null, eq(...) method in SpecificationEngine calls isNull(...)
        Predicate isNullPredicate = mock(Predicate.class);
        when(criteriaBuilder.isNull(numberPath)).thenReturn(isNullPredicate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertSame(isNullPredicate, result);
        verify(criteriaBuilder).isNull(numberPath);
    }

    @Test
    void testToPredicateNotEq() {
        Filter filter = new Filter("name", SearchOperator.NOT_EQ, "John");
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("name")).thenReturn(stringPath);

        Predicate notEqPredicate = mock(Predicate.class);
        when(criteriaBuilder.notEqual(stringPath, "John")).thenReturn(notEqPredicate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertSame(notEqPredicate, result);
        verify(criteriaBuilder).notEqual(stringPath, "John");
    }

    @Test
    void testToPredicateNotEq_withNullValue() {
        // If value is null, notEq(...) calls criteriaBuilder.isNotNull(...)
        Filter filter = new Filter("age", SearchOperator.NOT_EQ, null);
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("age")).thenReturn(numberPath);

        Predicate isNotNullPredicate = mock(Predicate.class);
        when(criteriaBuilder.isNotNull(numberPath)).thenReturn(isNotNullPredicate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertSame(isNotNullPredicate, result);
        verify(criteriaBuilder).isNotNull(numberPath);
    }

    @Test
    void testToPredicateGt() {
        Filter filter = new Filter("age", SearchOperator.GT, 18);
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("age")).thenReturn(comparablePath);

        Predicate greaterThanPredicate = mock(Predicate.class);
        when(criteriaBuilder.greaterThan(comparablePath, 18)).thenReturn(greaterThanPredicate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertSame(greaterThanPredicate, result);
        verify(criteriaBuilder).greaterThan(comparablePath, 18);
    }

    @Test
    void testToPredicateLt() {
        Filter filter = new Filter("age", SearchOperator.LT, 65);
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("age")).thenReturn(comparablePath);

        Predicate lessThanPredicate = mock(Predicate.class);
        when(criteriaBuilder.lessThan(comparablePath, 65)).thenReturn(lessThanPredicate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertSame(lessThanPredicate, result);
        verify(criteriaBuilder).lessThan(comparablePath, 65);
    }

    @Test
    void testToPredicateGte() {
        Filter filter = new Filter("age", SearchOperator.GTE, 10);
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("age")).thenReturn(comparablePath);

        Predicate gtePredicate = mock(Predicate.class);
        when(criteriaBuilder.greaterThanOrEqualTo(comparablePath, 10)).thenReturn(gtePredicate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertSame(gtePredicate, result);
        verify(criteriaBuilder).greaterThanOrEqualTo(comparablePath, 10);
    }

    @Test
    void testToPredicateLte() {
        Filter filter = new Filter("age", SearchOperator.LTE, 100);
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("age")).thenReturn(comparablePath);

        Predicate ltePredicate = mock(Predicate.class);
        when(criteriaBuilder.lessThanOrEqualTo(comparablePath, 100)).thenReturn(ltePredicate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertSame(ltePredicate, result);
        verify(criteriaBuilder).lessThanOrEqualTo(comparablePath, 100);
    }

    @Test
    void testToPredicateLike() {
        Filter filter = new Filter("name", SearchOperator.LIKE, "John");
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("name")).thenReturn(stringPath);

        // Must call fieldPath.as(String.class) inside the engine
        when(stringPath.as(String.class)).thenReturn(stringPath);

        Predicate likePredicate = mock(Predicate.class);
        when(criteriaBuilder.like(stringPath, "%John%")).thenReturn(likePredicate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertSame(likePredicate, result);
        verify(criteriaBuilder).like(stringPath, "%John%");
    }

    @Test
    void testToPredicateNotLike() {
        Filter filter = new Filter("name", SearchOperator.NOT_LIKE, "Jane");
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("name")).thenReturn(stringPath);
        when(stringPath.as(String.class)).thenReturn(stringPath);

        Predicate notLikePredicate = mock(Predicate.class);
        when(criteriaBuilder.notLike(stringPath, "%Jane%")).thenReturn(notLikePredicate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertSame(notLikePredicate, result);
        verify(criteriaBuilder).notLike(stringPath, "%Jane%");
    }

    @Test
    void testToPredicateBetween() {
        Filter filter = new Filter("price", SearchOperator.BETWEEN, List.of(10, 20));
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("price")).thenReturn(comparablePath);

        Predicate betweenPredicate = mock(Predicate.class);
        when(criteriaBuilder.between(comparablePath, 10, 20)).thenReturn(betweenPredicate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertSame(betweenPredicate, result);
        verify(criteriaBuilder).between(comparablePath, 10, 20);
    }

    @Test
    void testToPredicateIn() {
        Filter filter = new Filter("id", SearchOperator.IN, List.of(1, 2, 3));
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        when(root.get("id")).thenReturn(numberPath);

        // For .in(...) we typically get a CriteriaBuilder.In<Object>.
        CriteriaBuilder.In<Object> inClause = mock(CriteriaBuilder.In.class);

        // We'll mock how the engine creates and populates it
        when(criteriaBuilder.in(numberPath)).thenReturn(inClause);
        // For each .value(...) call, we can just return the inClause (builder pattern)
        when(inClause.value(any())).thenReturn(inClause);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // The 'inClause' is what the engine returns
        assertSame(inClause, result);
        verify(criteriaBuilder).in(numberPath);

        // Optionally verify it was called with each value
        verify(inClause).value(1);
        verify(inClause).value(2);
        verify(inClause).value(3);
    }

    @Test
    void testToPredicate_JoinNotFoundException() {
        // 1) Suppose we have an attribute with a dot: "department.manager"
        // but the root has no actual join named "department"
        // => triggers JoinNotFoundException

        Filter filter = new Filter("department.manager", SearchOperator.EQ, "Alice");
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        // root.getJoins() is empty => the code can't find "department"
        when(root.getJoins()).thenReturn(java.util.Collections.emptySet());

        // 2) Attempt to do toPredicate
        // Because "department" join is not found, we expect a JoinNotFoundException
        assertThrows(JoinNotFoundException.class,
                () -> spec.toPredicate(root, query, criteriaBuilder));
    }

    @Test
    void testToPredicate_AttributeNotFoundException() {
        // 1) Single attribute (no dot), but root.get("someField") fails
        // => triggers an AttributeNotFoundException

        Filter filter = new Filter("nonExistentField", SearchOperator.EQ, "Bob");
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        // 2) We can simulate root.get(...) throwing an exception
        when(root.get("nonExistentField")).thenThrow(new IllegalArgumentException("No such field"));

        // 3) We expect AttributeNotFoundException from SpecUtils
        assertThrows(AttributeNotFoundException.class,
                () -> spec.toPredicate(root, query, criteriaBuilder));
    }

    @Test
    void testToPredicate_JoinButAttributeNotFoundOnNestedPart() {
        Join<Dto, Object> mockJoin = mock(Join.class);

        // Suppose "department.manager.name"
        // The code finds a join for "department" but not for "manager"
        // or the next path call fails.
        Filter filter = new Filter("department.manager.name", SearchOperator.EQ, "Bob");
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        Attribute attr = mock(Attribute.class);
        when(attr.getName()).thenReturn("department");

        // root.getJoins() has a single join named "department"
        when(root.getJoins()).thenReturn(java.util.Set.of(mockJoin));
        // We mock the join's attribute name = "department"
        when(mockJoin.getAttribute()).thenReturn(attr);

        // Now, the code tries to do `mockJoin.get("manager")`,
        // let's say we throw an error or return null to trigger an "AttributeNotFoundException"
        when(mockJoin.get("manager")).thenThrow(new IllegalArgumentException("manager not found"));

        assertThrows(AttributeNotFoundException.class,
                () -> spec.toPredicate(root, query, criteriaBuilder));
    }

    @Test
    void testToPredicate_MappingException() {
        // We'll define a scenario: maybe the SpecUtils or SpecificationEngine
        // throws a "MappingException" if a type can't be cast to Comparable
        // for GT, LT, etc.
        Filter filter = new Filter("title", SearchOperator.GT, "someString");
        SpecificationWrapper<Dto> spec = new SpecificationWrapper<>(filter);

        // Suppose "title" is not a numeric or comparable field.
        // We'll mock an Object path that can't be cast to Expression<? extends Comparable>
        Path<String> titlePath = mock(Path.class);
        when(root.<String>get("title")).thenReturn(titlePath);

        // The code tries to do: (Expression<? extends Comparable>) titlePath
        // or `criteriaBuilder.greaterThan(titlePath, 20)` => leads to ClassCastException
        // We'll mimic that by just throwing a custom "MappingException"
        doThrow(new RuntimeException("MappingException: cannot compare strings to numeric")).when(criteriaBuilder)
                .greaterThan(any(Expression.class), (Comparable) any());

        // Now we can check for that specific runtime exception or your custom exception
        assertThrows(RuntimeException.class,
                () -> spec.toPredicate(root, query, criteriaBuilder),
                "Should throw a custom or runtime MappingException"
        );
    }
}

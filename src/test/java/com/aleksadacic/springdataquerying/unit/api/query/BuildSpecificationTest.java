package com.aleksadacic.springdataquerying.unit.api.query;

import com.aleksadacic.springdataquerying.api.Query;
import com.aleksadacic.springdataquerying.api.SearchOperator;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import utils.PersonEntity;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuildSpecificationTest extends FilterInclusionTest {

    /**
     * Demonstrates that Query.get() creates a new Query instance
     * with an empty (null) filter in the underlying specification.
     */
    @Test
    void testGetCreatesEmptyQuery() {
        Query<PersonEntity> query = Query.get();
        assertNotNull(query);
        Specification<PersonEntity> spec = query.buildSpecification();
        assertNotNull(spec);
    }

    /**
     * Demonstrates the simpler static where(String, Object) => uses EQ operator.
     */
    @Test
    void testWhereWithAttributeAndValue() {
        Query<PersonEntity> query = Query.where("name", "John");
        assertNotNull(query.buildSpecification());
    }

    /**
     * Demonstrates where(String, SearchOperator, Object).
     */
    @Test
    void testWhereWithAttributeOperatorValue() {
        Query<PersonEntity> query = Query.where("age", SearchOperator.GT, 18);
        assertNotNull(query.buildSpecification());
    }

    /**
     * Demonstrates adding multiple AND conditions and verifying the combination.
     */
    @Test
    void testAndCombination() {
        // 1) Build your Query
        Query<PersonEntity> query = Query.<PersonEntity>where("name", "John")
                .and("age", SearchOperator.GT, 18);

        Specification<PersonEntity> spec = query.buildSpecification();
        assertNotNull(spec);

        // 2) Stub the CriteriaQuery mock, so we don't get null from getRestriction()
        Predicate finalPredicate = stubCriteriaQuery(criteriaQuery);

        // 3) Prepare expectations
        List<FilterExpectation> filterExpectations = List.of(
                new FilterExpectation("name", SearchOperator.EQ, "John"),
                new FilterExpectation("age", SearchOperator.GT, 18));

        // 4) Verify
        verifyFilterInclusion(spec, filterExpectations, CombinationLogic.AND);

        // 5) Assert specification's predicate
        Predicate predicateFromSpec = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertSame(finalPredicate, predicateFromSpec, "Should return the stubbed final predicate");
    }

    /**
     * Demonstrates adding multiple OR conditions and verifying the combination.
     */

    @Test
    void testOrCombination() {
        // 1) Build your Query
        Query<PersonEntity> query = Query.<PersonEntity>where("name", "John")
                .or("age", SearchOperator.GT, 18);

        Specification<PersonEntity> spec = query.buildSpecification();
        assertNotNull(spec);

        // 2) Stub the CriteriaQuery mock, so we don't get null from getRestriction()
        Predicate finalPredicate = stubCriteriaQuery(criteriaQuery);

        // 3) Prepare expectations
        List<FilterExpectation> filterExpectations = List.of(
                new FilterExpectation("name", SearchOperator.EQ, "John"),
                new FilterExpectation("age", SearchOperator.GT, 18));

        // 4) Verify
        verifyFilterInclusion(spec, filterExpectations, CombinationLogic.OR);

        // 5) Assert specification's predicate
        Predicate predicateFromSpec = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertSame(finalPredicate, predicateFromSpec, "Should return the stubbed final predicate");
    }

    /**
     * Demonstrates distinct() sets the distinct flag. We verify that the resulting
     * CriteriaQuery is set to distinct in buildSpecification().
     */
    @Test
    void testDistinct() {
        when(criteriaQuery.distinct(true)).thenReturn(criteriaQuery);

        Query<PersonEntity> query = Query.<PersonEntity>where("name", "John").distinct();
        Specification<PersonEntity> spec = query.buildSpecification();
        spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        verify(criteriaQuery).distinct(true);
    }

    /**
     * Demonstrates join() usage with a single attribute (e.g., "department").
     */
    @Test
    void testJoinSingleAttribute() {
        Query<PersonEntity> query = Query.<PersonEntity>where("name", "John").join("department", JoinType.LEFT);

        // As soon as we run buildSpecification and toPredicate, the join logic is triggered.
        Specification<PersonEntity> spec = query.buildSpecification();
        spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        // We'll verify that root.join(...) was called once with "department" & LEFT join.
        verify(root, times(1)).join("department", JoinType.LEFT);
    }

    /**
     * Demonstrates join() usage with a nested attribute, e.g. "department.manager".
     * The code should call root.join("department", ...), then join.join("manager", ...).
     */
    @Test
    void testJoinNestedAttribute() {
        Query<PersonEntity> query = Query.<PersonEntity>where("name", "John")
                .join("department.manager", jakarta.persistence.criteria.JoinType.LEFT);

        Specification<PersonEntity> spec = query.buildSpecification();

        // We need a mock Join to return from root.join(...).
        @SuppressWarnings("unchecked")
        Join<Object, Object> mockDepartmentJoin = mock(Join.class);
        when(root.join("department", jakarta.persistence.criteria.JoinType.LEFT))
                .thenReturn(mockDepartmentJoin);

        // If the second join is manager, that call will be on mockDepartmentJoin.join("manager", ...).
        @SuppressWarnings("unchecked")
        Join<Object, Object> mockManagerJoin = mock(Join.class);
        when(mockDepartmentJoin.join("manager", jakarta.persistence.criteria.JoinType.LEFT))
                .thenReturn(mockManagerJoin);

        spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        // Verify calls
        verify(root, times(1)).join("department", jakarta.persistence.criteria.JoinType.LEFT);
        verify(mockDepartmentJoin, times(1)).join("manager", jakarta.persistence.criteria.JoinType.LEFT);
    }

    /**
     * Demonstrates join() usage with a nested attribute, e.g. "department.manager".
     * Attribute name in Department is being filtered.
     * The code should call root.join("department", ...), then join.join("manager", ...).
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testJoinNestedAttributeWithFilteringNestedAttribute() {
        @SuppressWarnings("unchecked")
        Root<Object> root = mock(Root.class);

        Query<Object> query = Query.where("department.manager.name", "John")
                .join("department.manager", jakarta.persistence.criteria.JoinType.LEFT);

        Specification<Object> spec = query.buildSpecification();

        // We need a mock Join to return from root.join(...).
        Join<Object, Object> mockDepartmentJoin = mock(Join.class);
        when(root.join("department", jakarta.persistence.criteria.JoinType.LEFT))
                .thenReturn(mockDepartmentJoin);

        // If the second join is manager, that call will be on mockDepartmentJoin.join("manager", ...).
        Join<Object, Object> mockManagerJoin = mock(Join.class);
        when(mockDepartmentJoin.join("manager", jakarta.persistence.criteria.JoinType.LEFT))
                .thenReturn(mockManagerJoin);

        Set<Join<Object, ?>> joinSet = Set.of(mockDepartmentJoin);
        when(root.getJoins()).thenReturn(joinSet);
        Attribute attribute = mock(Attribute.class);
        when(mockDepartmentJoin.getAttribute()).thenReturn(attribute);
        when(attribute.getName()).thenReturn("department");
        when(mockDepartmentJoin.get("manager")).thenReturn(mockManagerJoin);

        spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        // Verify calls
        verify(root, times(1)).join("department", jakarta.persistence.criteria.JoinType.LEFT);
        verify(mockDepartmentJoin, times(1)).join("manager", jakarta.persistence.criteria.JoinType.LEFT);

        verify(root, times(1)).getJoins();
        verify(mockDepartmentJoin, times(1)).get("manager");
    }

    /**
     * Demonstrates buildSpecification returns null predicate if no filters.
     * We create a Query with no conditions and call buildSpecification.
     * Then we check that the final returned predicate is null (indicating no restrictions).
     */
    @Test
    void testBuildSpecificationNoPredicates() {
        Query<PersonEntity> query = Query.get();
        Specification<PersonEntity> spec = query.buildSpecification();

        Predicate predicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertNull(predicate, "No predicates added, so we expect null or trivial predicate.");
    }

    /**
     * Demonstrates buildSpecification with a single condition
     * that yields a non-trivial predicate.
     */
    @Test
    void testBuildSpecificationWithPredicates() {
        // 1) Build the Query and Specification
        Query<PersonEntity> query = Query.where("age", 30);
        Specification<PersonEntity> spec = query.buildSpecification();
        assertNotNull(spec, "Specification should not be null");

        // 2) Stub the criteriaQuery to avoid null returns
        stubCriteriaQuery(criteriaQuery);

        // 3) Create a single FilterExpectation for "age" == 30
        List<FilterExpectation> expectations = List.of(
                new FilterExpectation("age", SearchOperator.EQ, 30)
        );

        // 4) Verify filter inclusion (no combination, just a single filter => NONE)
        verifyFilterInclusion(spec, expectations, CombinationLogic.NONE);
    }

    /**
     * Tests currently support ONLY THIS chain combination: where -> and -> or.
     */
    @Test
    void testChainFiltering() {
        // 1) Build the actual query
        Query<PersonEntity> query = Query.<PersonEntity>where("age", 30)
                .and("name", "John")
                .or("height", SearchOperator.GT, 180);

        // 2) Convert to specification
        Specification<PersonEntity> spec = query.buildSpecification();
        assertNotNull(spec, "Spec should not be null");

        // 3) Stub the criteriaQuery, so it doesn't return null
        stubCriteriaQuery(criteriaQuery);

        // 4) Provide the EXACT 3 filters in a List
        List<FilterExpectation> chainFilters = List.of(
                new FilterExpectation("age", SearchOperator.EQ, 30),
                new FilterExpectation("name", SearchOperator.EQ, "John"),
                new FilterExpectation("height", SearchOperator.GT, 180)
        );

        // 5) Pass them to verifyFilterInclusion with CHAIN
        verifyFilterInclusion(spec, chainFilters, CombinationLogic.WHERE_AND_OR);
    }
}

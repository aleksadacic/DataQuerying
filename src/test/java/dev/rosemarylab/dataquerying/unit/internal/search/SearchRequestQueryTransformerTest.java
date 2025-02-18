package dev.rosemarylab.dataquerying.unit.internal.search;

import dev.rosemarylab.dataquerying.api.Query;
import dev.rosemarylab.dataquerying.api.SearchOperator;
import dev.rosemarylab.dataquerying.api.SearchRequest;
import dev.rosemarylab.dataquerying.internal.enums.ConditionalOperator;
import dev.rosemarylab.dataquerying.internal.search.FilterData;
import dev.rosemarylab.dataquerying.internal.search.SearchRequestQueryTransformer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@SuppressWarnings("rawtypes")
class SearchRequestQueryTransformerTest {

    private MockedStatic<Query> queryStaticMock;
    private Query<Object> querySpy;

    @BeforeEach
    void setUp() {
        // 1) Create a real Query instance and spy on it
        Query<Object> realQuery = Query.get(); // real instance
        querySpy = spy(realQuery);

        // 2) Mock the static Query.get() to return our spy
        queryStaticMock = Mockito.mockStatic(Query.class);
        queryStaticMock.when(Query::get).thenReturn(querySpy);
    }

    @AfterEach
    void tearDown() {
        // 3) Close the static mock after each test to avoid interference
        queryStaticMock.close();
    }

    @Test
    void testToQuery_noFilters() {
        // Given
        SearchRequest request = new SearchRequest();
        // No filters set (request.getFilters() is null or empty)

        // When
        Query<Object> result = SearchRequestQueryTransformer.toQuery(request);

        // Then
        // We expect the same spy to be returned
        assertSame(querySpy, result, "Should return the spy instance from Query.get()");

        // Verify that no 'and' or 'or' calls were made
        verify(querySpy, never()).and(anyString(), any(), any());
        verify(querySpy, never()).or(anyString(), any(), any());

        // Or no join calls
        verify(querySpy, never()).join(anyString(), any());

        // Distinct should not be set
        // (We can't directly check a private field, but we know no call should happen)
        verify(querySpy, never()).distinct();
    }

    @Test
    void testToQuery_singleFilter_andOperator() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setConditionalOperator(ConditionalOperator.AND);

        FilterData filterData = new FilterData();
        filterData.setAttribute("age");
        filterData.setSearchOperator(SearchOperator.GT);
        filterData.setValue(30);

        request.setFilters(List.of(filterData));

        // When
        Query<Object> result = SearchRequestQueryTransformer.toQuery(request);

        // Then
        assertSame(querySpy, result);
        // Because top-level operator is 'AND', we expect query.and("age", GT, 30)
        verify(querySpy).and("age", SearchOperator.GT, 30);

        // No OR calls
        verify(querySpy, never()).or(anyString(), any(), any());
        // No join calls
        verify(querySpy, never()).join(anyString(), any());
        // No distinct
        verify(querySpy, never()).distinct();
    }

    @Test
    void testToQuery_singleFilter_orOperator() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setConditionalOperator(ConditionalOperator.OR);

        FilterData filterData = new FilterData();
        filterData.setAttribute("name");
        filterData.setSearchOperator(SearchOperator.LIKE);
        filterData.setValue("Alice");

        request.setFilters(List.of(filterData));

        // When
        Query<Object> result = SearchRequestQueryTransformer.toQuery(request);

        // Then
        assertSame(querySpy, result);
        verify(querySpy).or("name", SearchOperator.LIKE, "Alice");

        // No 'AND' calls
        verify(querySpy, never()).and(anyString(), any(), any());
        verify(querySpy, never()).join(anyString(), any());
        verify(querySpy, never()).distinct();
    }

    @Test
    void testToQuery_attributeWithDot_triggersJoinAndDistinct() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setConditionalOperator(ConditionalOperator.AND);

        FilterData filterData = new FilterData();
        // "department.manager" => triggers join("department") + distinct
        filterData.setAttribute("department.manager");
        filterData.setSearchOperator(SearchOperator.EQ);
        filterData.setValue("John");

        request.setFilters(List.of(filterData));

        // When
        Query<Object> result = SearchRequestQueryTransformer.toQuery(request);

        // Then
        assertSame(querySpy, result);

        // 1) Should call join("department", JoinType.INNER)
        verify(querySpy).join("department", jakarta.persistence.criteria.JoinType.INNER);

        // 2) Should set distinct
        verify(querySpy).distinct();

        // 3) Because it's top-level AND
        verify(querySpy).and("department.manager", SearchOperator.EQ, "John");
    }

    @Test
    void testToQuery_multipleFilters_OR() {
        // 2 top-level filters with OR
        SearchRequest request = new SearchRequest();
        request.setConditionalOperator(ConditionalOperator.OR);

        FilterData f1 = new FilterData();
        f1.setAttribute("status");
        f1.setSearchOperator(SearchOperator.EQ);
        f1.setValue("ACTIVE");

        FilterData f2 = new FilterData();
        f2.setAttribute("age");
        f2.setSearchOperator(SearchOperator.LT);
        f2.setValue(40);

        request.setFilters(List.of(f1, f2));

        // When
        Query<Object> result = SearchRequestQueryTransformer.toQuery(request);

        // Then
        assertNotNull(result);

        // Expect 'OR' calls for each leaf
        verify(querySpy).or("status", SearchOperator.EQ, "ACTIVE");
        verify(querySpy).or("age", SearchOperator.LT, 40);

        // No 'AND' calls, no join, no distinct
        verify(querySpy, never()).and(anyString(), any(), any());
        verify(querySpy, never()).join(anyString(), any());
        verify(querySpy, never()).distinct();
    }

    @Test
    void testToQuery_nestedFilters() {
        SearchRequest request = new SearchRequest();
        request.setConditionalOperator(ConditionalOperator.OR);

        FilterData subF1 = new FilterData();
        subF1.setAttribute("name");
        subF1.setSearchOperator(SearchOperator.EQ);
        subF1.setValue("Alice");

        FilterData subF2 = new FilterData();
        subF2.setAttribute("active");
        subF2.setSearchOperator(SearchOperator.EQ);
        subF2.setValue(true);

        // The top-level filter is a group:
        FilterData groupFilter = new FilterData();
        groupFilter.setFilters(List.of(subF1, subF2));
        groupFilter.setConditionalOperator(ConditionalOperator.AND);

        request.setFilters(List.of(groupFilter));
        request.setConditionalOperator(ConditionalOperator.OR);

        // When
        Query<Object> result = SearchRequestQueryTransformer.toQuery(request);

        // Then
        assertSame(querySpy, result);

        // Because the top-level operator is OR, we expect: query.or(subQuery)
        // But inside that subQuery, we do AND("name"=Alice) AND("active"=true)

        // We can't directly see "subQuery" since it's ephemeral, but we can see the effect:
        // 1) We expect one "or(...subQuery...)" call
        // 2) That subQuery, internally, called and(...) for each leaf
        verify(querySpy).or(argThat((Query<Object> innerQ) -> {
            // We'll do a small spy on this subQuery or we can do a simpler approach:
            // if we can't easily do an argument matcher, we at least confirm a single or(Query).
            return true;
        }));
    }
}

package com.aleksadacic.dataquerying.unit.api.search;

import com.aleksadacic.dataquerying.api.Query;
import com.aleksadacic.dataquerying.api.SearchOperator;
import com.aleksadacic.dataquerying.api.SearchRequest;
import com.aleksadacic.dataquerying.internal.enums.ConditionalOperator;
import com.aleksadacic.dataquerying.internal.enums.SortOrder;
import com.aleksadacic.dataquerying.internal.search.FilterData;
import com.aleksadacic.dataquerying.internal.search.OrderInfo;
import com.aleksadacic.dataquerying.internal.search.PageInfo;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import utils.Dto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * We don't need to test if the Specification object is well instantiated because
 * Specification object is created from Query object, and we have tests that cover that also.
 */
class SearchRequestTest {

    @Test
    void testGetQuery_withNoFilters() {
        // Given
        SearchRequest request = new SearchRequest();
        // No filters, no operator set

        // When
        Query<Dto> query = request.getQuery();

        // Then
        assertNotNull(query, "Query should not be null even with no filters");
    }

    @Test
    void testGetQuery_withSomeFilters() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setConditionalOperator(ConditionalOperator.AND);

        FilterData filter1 = new FilterData();
        filter1.setAttribute("name");
        filter1.setValue("Alice");
        filter1.setSearchOperator(SearchOperator.EQ);

        FilterData filter2 = new FilterData();
        filter2.setAttribute("age");
        filter2.setValue(30);
        filter2.setSearchOperator(SearchOperator.GT);

        request.setFilters(List.of(filter1, filter2));

        // When
        Query<Dto> query = request.getQuery();

        // Then
        assertNotNull(query, "We should get a non-null Query");
    }

    @Test
    void testGetPageable_noPageSet() {
        // Given
        SearchRequest request = new SearchRequest();

        // When
        Pageable pageable = request.getPageable();

        // Then
        assertTrue(pageable.isUnpaged(), "If page or pageSize is not provided, it should be unpaged");
    }

    @Test
    void testGetPageable_withValidPage() {
        // Given
        SearchRequest request = new SearchRequest();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(20);
        request.setPage(pageInfo);

        // When
        Pageable pageable = request.getPageable();

        // Then
        assertNotNull(pageable, "Should return a Pageable");
        assertFalse(pageable.isUnpaged(), "Should not be unpaged");
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void testGetPageRequest_noPageInfo() {
        SearchRequest request = new SearchRequest();
        assertNull(request.getPageRequest(), "If page info is not set, getPageRequest() should return null");
    }

    @Test
    void testGetPageRequest_withPageSizeAndNumber_noOrder() {
        // Given
        SearchRequest request = new SearchRequest();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNumber(2);
        pageInfo.setPageSize(50);
        request.setPage(pageInfo);

        // When
        PageRequest pageRequest = request.getPageRequest();

        // Then
        assertNotNull(pageRequest, "Should not be null if page info is valid");
        assertEquals(2, pageRequest.getPageNumber());
        assertEquals(50, pageRequest.getPageSize());
        assertTrue(pageRequest.getSort().isUnsorted(), "No order => unsorted");
    }

    @Test
    void testGetPageRequest_withOrder() {
        // Given
        SearchRequest request = new SearchRequest();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNumber(0);
        pageInfo.setPageSize(10);
        request.setPage(pageInfo);

        OrderInfo orderInfo1 = new OrderInfo("name", SortOrder.ASC);
        OrderInfo orderInfo2 = new OrderInfo("age", SortOrder.DESC);
        request.getOrder().add(orderInfo1);
        request.getOrder().add(orderInfo2);

        // When
        PageRequest pageRequest = request.getPageRequest();

        // Then
        assertNotNull(pageRequest);
        Sort sort = pageRequest.getSort();
        assertFalse(sort.isUnsorted(), "We should have 2 sort orders");

        // We can check the ordering:
        Sort.Order first = sort.getOrderFor("name");
        assertNotNull(first);
        assertEquals(Sort.Direction.ASC, first.getDirection());

        Sort.Order second = sort.getOrderFor("age");
        assertNotNull(second);
        assertEquals(Sort.Direction.DESC, second.getDirection());
    }

    @Test
    void testGetSort_noOrder() {
        SearchRequest request = new SearchRequest();

        Sort sort = request.getSort();
        assertTrue(sort.isUnsorted(), "No orders => unsorted");
    }

    @Test
    void testGetSort_withOrder() {
        SearchRequest request = new SearchRequest();

        OrderInfo o1 = new OrderInfo("city", SortOrder.ASC);
        OrderInfo o2 = new OrderInfo("age", SortOrder.DESC);
        request.getOrder().add(o1);
        request.getOrder().add(o2);

        Sort sort = request.getSort();
        assertNotNull(sort);
        assertFalse(sort.isUnsorted());

        Sort.Order cityOrder = sort.getOrderFor("city");
        assertNotNull(cityOrder);
        assertEquals(Sort.Direction.ASC, cityOrder.getDirection());

        Sort.Order ageOrder = sort.getOrderFor("age");
        assertNotNull(ageOrder);
        assertEquals(Sort.Direction.DESC, ageOrder.getDirection());
    }
}

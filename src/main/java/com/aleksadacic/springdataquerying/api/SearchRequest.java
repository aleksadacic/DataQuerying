package com.aleksadacic.springdataquerying.api;

import com.aleksadacic.springdataquerying.internal.enums.ConditionalOperator;
import com.aleksadacic.springdataquerying.internal.search.FilterData;
import com.aleksadacic.springdataquerying.internal.search.OrderInfo;
import com.aleksadacic.springdataquerying.internal.search.PageInfo;
import com.aleksadacic.springdataquerying.internal.search.SearchRequestQueryTransformer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SearchRequest {
    private List<FilterData> filters; // List of filters, supports nesting
    private ConditionalOperator conditionalOperator; // AND, OR for a filter group
    private PageInfo page; // Pagination information
    private List<OrderInfo> order = new ArrayList<>(); // Order information

    @SuppressWarnings("unused")
    @JsonIgnore
    public <T> Specification<T> getSpecification() {
        return SearchRequestQueryTransformer.<T>toQuery(this).buildSpecification();
    }

    @SuppressWarnings("unused")
    @JsonIgnore
    public <T> Query<T> getQuery() {
        return SearchRequestQueryTransformer.toQuery(this);
    }

    @SuppressWarnings("unused")
    @JsonIgnore
    public Pageable getPageable() {
        if (page != null && page.getPageSize() != null) {
            return Pageable.ofSize(page.getPageSize());
        }
        return Pageable.unpaged();
    }

    @SuppressWarnings("unused")
    @JsonIgnore
    public PageRequest getPageRequest() {
        PageRequest pageRequest = null;
        if (page != null && page.getPageNumber() != null && page.getPageSize() != null) {
            pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize());
        }

        if (pageRequest == null)
            return null;

        List<Sort.Order> orders = new ArrayList<>();
        for (OrderInfo orderInfo : order) {
            Sort.Direction direction = Sort.Direction.valueOf(orderInfo.getSortOrder().value);
            orders.add(new Sort.Order(direction, orderInfo.getAttribute()));
        }
        return pageRequest.withSort(Sort.by(orders));
    }

    @SuppressWarnings("unused")
    @JsonIgnore
    public Sort getSort() {
        List<Sort.Order> orders = new ArrayList<>();
        for (OrderInfo orderInfo : order) {
            Sort.Direction direction = Sort.Direction.valueOf(orderInfo.getSortOrder().value);
            orders.add(new Sort.Order(direction, orderInfo.getAttribute()));
        }
        return Sort.by(orders);
    }
}

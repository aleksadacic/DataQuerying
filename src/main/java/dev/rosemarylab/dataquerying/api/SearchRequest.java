package dev.rosemarylab.dataquerying.api;

import dev.rosemarylab.dataquerying.internal.enums.ConditionalOperator;
import dev.rosemarylab.dataquerying.internal.search.FilterData;
import dev.rosemarylab.dataquerying.internal.search.OrderInfo;
import dev.rosemarylab.dataquerying.internal.search.PageInfo;
import dev.rosemarylab.dataquerying.internal.search.SearchRequestQueryTransformer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a search request that encapsulates filtering, sorting, and pagination details
 * for querying data. It should be mainly used as a REST service request body.
 * <br><br>
 * This class is designed to handle complex query structures, including:
 * <ul>
 *     <li>Nested filters with conditional operators (AND/OR)</li>
 *     <li>Sorting based on multiple fields with specified directions</li>
 *     <li>Pagination parameters for efficient data retrieval</li>
 * </ul>
 * <p>
 * This class provides helper methods to generate Spring Data constructs such as
 * {@link org.springframework.data.jpa.domain.Specification}, {@link Pageable},
 * and {@link Sort} for seamless integration with JPA repositories.
 * </p>
 */
@Setter
@Getter
public class SearchRequest {
    private List<FilterData> filters; // List of filters, supports nesting
    private ConditionalOperator conditionalOperator; // AND, OR for a filter group
    private PageInfo page; // Pagination information
    private List<OrderInfo> order = new ArrayList<>(); // Order information

    /**
     * Converts the search request into a JPA {@link Specification}, which represents
     * the query criteria for the underlying entity.
     *
     * @param <T> The type of the entity being queried.
     * @return The {@link Specification} representing the query criteria.
     */
    @JsonIgnore
    public <T> Specification<T> getSpecification() {
        return SearchRequestQueryTransformer.<T>toQuery(this).buildSpecification();
    }

    /**
     * Converts the search request into a {@link Query} object, allowing for more dynamic
     * query building and execution.
     *
     * @param <T> The type of the entity being queried.
     * @return The {@link Query} representing the query criteria.
     */
    @JsonIgnore
    public <T> Query<T> getQuery() {
        return SearchRequestQueryTransformer.toQuery(this);
    }

    /**
     * Converts the search request into a Spring Data {@link Pageable} object, which includes
     * pagination and sorting information. If pagination details are not specified, it defaults
     * to {@link Pageable#unpaged()}.
     *
     * @return The {@link Pageable} object representing pagination and sorting details.
     */
    @JsonIgnore
    public Pageable getPageable() {
        PageRequest pageRequest = getPageRequest();
        if (pageRequest == null) return Pageable.unpaged();
        return pageRequest;
    }

    /**
     * Creates a {@link PageRequest} object based on the pagination details (page number and size)
     * and sorting information. If pagination details are not provided, it returns {@code null}.
     *
     * @return The {@link PageRequest} object representing pagination and sorting details, or {@code null} if not specified.
     */
    @JsonIgnore
    public PageRequest getPageRequest() {
        PageRequest pageRequest = null;
        if (page != null && page.getPageSize() != null) {
            pageRequest = PageRequest.ofSize(page.getPageSize());
            if (page.getPageNumber() != null) {
                pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize());
            }
        }

        if (pageRequest == null)
            return null;

        return pageRequest.withSort(getSort());
    }

    /**
     * Converts the sorting details in the search request into a {@link Sort} object.
     * Each {@link OrderInfo} entry specifies the attribute to sort by and the direction
     * (ascending or descending).
     *
     * @return The {@link Sort} object representing the sorting criteria.
     */
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

package dev.rosemarylab.dataquerying.internal.search;

import dev.rosemarylab.dataquerying.internal.enums.ConditionalOperator;
import dev.rosemarylab.dataquerying.api.SearchOperator;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class FilterData {
    private String attribute;
    private Object value;
    private SearchOperator searchOperator; // EQ, NOT_EQ, GTE, LTE, IN, LIKE, etc.
    private List<FilterData> filters; // nested filters
    private ConditionalOperator conditionalOperator; // AND, OR for a filter group
}

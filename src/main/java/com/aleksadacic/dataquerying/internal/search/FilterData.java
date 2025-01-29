package com.aleksadacic.dataquerying.internal.search;

import com.aleksadacic.dataquerying.internal.enums.ConditionalOperator;
import com.aleksadacic.dataquerying.internal.enums.DataType;
import com.aleksadacic.dataquerying.api.SearchOperator;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class FilterData {
    private String attribute;
    private Object value;
    private SearchOperator searchOperator; // EQ, NOT_EQ, GTE, LTE, IN, LIKE, etc.
    private DataType type; // STRING, NUMBER, DATE, ARRAY, etc.
    private List<FilterData> filters; // nested filters
    private ConditionalOperator conditionalOperator; // AND, OR for a filter group
}

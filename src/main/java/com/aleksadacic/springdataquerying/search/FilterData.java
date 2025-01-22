package com.aleksadacic.springdataquerying.search;

import com.aleksadacic.springdataquerying.enums.ConditionalOperator;
import com.aleksadacic.springdataquerying.enums.DataType;
import com.aleksadacic.springdataquerying.enums.SearchOperator;
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

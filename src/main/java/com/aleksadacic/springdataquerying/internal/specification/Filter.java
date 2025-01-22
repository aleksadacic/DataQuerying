package com.aleksadacic.springdataquerying.internal.specification;

import com.aleksadacic.springdataquerying.api.SearchOperator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Filter {
    private SearchOperator operator;
    private Object value;
    private String attribute;

    public Filter(String attribute, SearchOperator operator, Object value) {
        this.operator = operator;
        this.value = value;
        this.attribute = attribute;
    }
}

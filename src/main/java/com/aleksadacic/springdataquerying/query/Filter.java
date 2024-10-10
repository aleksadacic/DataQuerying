package com.aleksadacic.springdataquerying;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Filter {
    private FilterOperator operator;
    private Object value;
    private String attribute;

    public Filter(String attribute, FilterOperator operator, Object value) {
        this.operator = operator;
        this.value = value;
        this.attribute = attribute;
    }
}

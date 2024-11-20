package com.aleksadacic.springdataquerying.data;

import com.aleksadacic.springdataquerying.query.FilterOperator;

public class FilterData {
    private String field;
    private Object value;
    private FilterOperator operator; // EQ, NOT_EQ, GTE, LTE, IN, LIKE, etc.
    private DataType type; // STRING, NUMBER, DATE, ARRAY, etc.

    // Getters and Setters

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public void setOperator(FilterOperator operator) {
        this.operator = operator;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }
}

package com.aleksadacic.springdataquerying.data;

import java.util.List;

public class ConditionGroup {
    private List<FilterData> filters;
    private List<ConditionGroup> nestedGroups; // Allow nesting further groups
    private LogicalOperator logicalOperator; // AND or OR

    // Getters and Setters

    public List<FilterData> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterData> filters) {
        this.filters = filters;
    }

    public List<ConditionGroup> getNestedGroups() {
        return nestedGroups;
    }

    public void setNestedGroups(List<ConditionGroup> nestedGroups) {
        this.nestedGroups = nestedGroups;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }
}

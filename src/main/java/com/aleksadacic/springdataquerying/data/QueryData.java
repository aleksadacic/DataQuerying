package com.aleksadacic.springdataquerying.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@Getter
public class QueryData<T> {
    private List<FilterData> filters; // List of filters
    private List<ConditionGroup> conditionGroups; // Nested conditions

    public void setFilters(List<FilterData> filters) {
        this.filters = filters;
    }

    public void setConditionGroups(List<ConditionGroup> conditionGroups) {
        this.conditionGroups = conditionGroups;
    }

    @JsonIgnore
    public Specification<T> getSpecification() {
        QuerySpecificationBuilder<T> builder = new QuerySpecificationBuilder<>();
        return builder.buildSpecification(this);
    }
}

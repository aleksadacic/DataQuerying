package com.aleksadacic.springdataquerying.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@Setter
@Getter
public class QueryData<T> {
    private List<FilterData> filters; // List of filters
    private List<ConditionGroup> conditionGroups; // Nested conditions

    @JsonIgnore
    public Specification<T> getSpecification() {
        return new QuerySpecificationBuilder<T>().buildSpecification(this);
    }
}

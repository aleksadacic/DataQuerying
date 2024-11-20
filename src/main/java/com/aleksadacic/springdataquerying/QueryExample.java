package com.aleksadacic.springdataquerying;

import com.aleksadacic.springdataquerying.data.*;
import com.aleksadacic.springdataquerying.query.FilterOperator;

import java.util.Arrays;

public class QueryExample {
    public static void main(String[] args) {
        // Create individual filters
        FilterData filter1 = new FilterData();
        filter1.setField("name");
        filter1.setOperator(FilterOperator.LIKE);
        filter1.setValue("John");
        filter1.setType(DataType.STRING);

        FilterData filter2 = new FilterData();
        filter2.setField("age");
        filter2.setOperator(FilterOperator.GTE);
        filter2.setValue(18);
        filter2.setType(DataType.NUMBER);

        // Create a condition group for combining filters with AND
        ConditionGroup group1 = new ConditionGroup();
        group1.setFilters(Arrays.asList(filter1, filter2));
        group1.setLogicalOperator(LogicalOperator.AND);

        // Create nested filters
        FilterData filter3 = new FilterData();
        filter3.setField("createdDate");
        filter3.setOperator(FilterOperator.BETWEEN);
        filter3.setValue(Arrays.asList("2023-01-01", "2023-12-31"));
        filter3.setType(DataType.DATE);

        ConditionGroup group2 = new ConditionGroup();
        group2.setFilters(Arrays.asList(filter3));
        group2.setLogicalOperator(LogicalOperator.AND);

        // Combine condition groups in the main QueryData
        QueryData queryData = new QueryData();
        queryData.setConditionGroups(Arrays.asList(group1, group2));

        // Now queryData can be used to parse a request body and generate a query
    }
}

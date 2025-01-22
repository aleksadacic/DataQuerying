package com.aleksadacic.springdataquerying.search;

import lombok.Data;

@Data
public class OrderInfo {
    private String attribute; // Attribute to sort by
    private SortOrder sortOrder; // ASC, DESC
}

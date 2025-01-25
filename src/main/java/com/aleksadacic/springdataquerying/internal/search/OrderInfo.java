package com.aleksadacic.springdataquerying.internal.search;

import com.aleksadacic.springdataquerying.internal.enums.SortOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {
    private String attribute; // Attribute to sort by
    private SortOrder sortOrder; // ASC, DESC
}

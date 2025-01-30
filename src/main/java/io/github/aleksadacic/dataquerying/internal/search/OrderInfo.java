package io.github.aleksadacic.dataquerying.internal.search;

import io.github.aleksadacic.dataquerying.internal.enums.SortOrder;
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

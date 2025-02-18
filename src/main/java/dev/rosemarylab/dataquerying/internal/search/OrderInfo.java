package dev.rosemarylab.dataquerying.internal.search;

import dev.rosemarylab.dataquerying.internal.enums.SortOrder;
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

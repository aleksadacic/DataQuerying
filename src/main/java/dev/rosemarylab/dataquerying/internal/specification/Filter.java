package dev.rosemarylab.dataquerying.internal.specification;

import dev.rosemarylab.dataquerying.api.SearchOperator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Filter {
    private SearchOperator operator;
    private Object value;
    private String attribute;

    public Filter(String attribute, SearchOperator operator, Object value) {
        this.operator = operator;
        this.value = value;
        this.attribute = attribute;
    }
}

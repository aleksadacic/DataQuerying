package dev.rosemarylab.dataquerying.internal.enums;

import dev.rosemarylab.dataquerying.internal.deserializers.ConditionalOperatorDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ConditionalOperatorDeserializer.class)
public enum ConditionalOperator {
    AND("AND"),
    OR("OR");

    public final String operator;

    ConditionalOperator(String operator) {
        this.operator = operator;
    }
}

package io.github.aleksadacic.dataquerying.internal.deserializers;

import io.github.aleksadacic.dataquerying.internal.enums.ConditionalOperator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ConditionalOperatorDeserializer extends JsonDeserializer<ConditionalOperator> {
    @Override
    public ConditionalOperator deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String operator = jsonParser.getText();

        for (ConditionalOperator conditionalOperator : ConditionalOperator.values()) {
            if (conditionalOperator.operator.equalsIgnoreCase(operator)) {
                return conditionalOperator;
            }
        }

        throw new IllegalArgumentException("Invalid ConditionalOperator: " + operator);
    }
}

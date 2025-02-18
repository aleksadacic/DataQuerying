package dev.rosemarylab.dataquerying.internal.deserializers;

import dev.rosemarylab.dataquerying.api.SearchOperator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class SearchOperatorDeserializer extends JsonDeserializer<SearchOperator> {
    @Override
    public SearchOperator deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String type = jsonParser.getText();

        for (SearchOperator searchOperator : SearchOperator.values()) {
            if (searchOperator.operator.equalsIgnoreCase(type)) {
                return searchOperator;
            }
        }

        throw new IllegalArgumentException("Invalid SearchOperator: " + type);
    }
}

package io.github.aleksadacic.dataquerying.internal.deserializers;

import io.github.aleksadacic.dataquerying.internal.enums.SortOrder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class SortOrderDeserializer extends JsonDeserializer<SortOrder> {
    @Override
    public SortOrder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String type = jsonParser.getText();

        for (SortOrder sortOrder : SortOrder.values()) {
            if (sortOrder.value.equalsIgnoreCase(type)) {
                return sortOrder;
            }
        }

        throw new IllegalArgumentException("Invalid SortOrder: " + type);
    }
}

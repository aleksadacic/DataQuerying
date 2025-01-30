package io.github.aleksadacic.dataquerying.internal.deserializers;

import io.github.aleksadacic.dataquerying.internal.enums.DataType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class DataTypeDeserializer extends JsonDeserializer<DataType> {
    @Override
    public DataType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String type = jsonParser.getText();

        for (DataType dataType : DataType.values()) {
            if (dataType.value.equalsIgnoreCase(type)) {
                return dataType;
            }
        }

        throw new IllegalArgumentException("Invalid DataType: " + type);
    }
}

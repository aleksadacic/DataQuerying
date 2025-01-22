package com.aleksadacic.springdataquerying.internal.deserializers;

import com.aleksadacic.springdataquerying.internal.enums.DataType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

@SuppressWarnings("unused")
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

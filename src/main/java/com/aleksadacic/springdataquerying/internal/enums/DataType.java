package com.aleksadacic.springdataquerying.internal.enums;

import com.aleksadacic.springdataquerying.internal.deserializers.DataTypeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = DataTypeDeserializer.class)
public enum DataType {
    STRING("STRING"),
    NUMBER("NUMBER"),
    DATE("DATE"),
    ARRAY("ARRAY"),
    BOOLEAN("BOOLEAN");

    public final String value;

    DataType(String value) {
        this.value = value;
    }
}

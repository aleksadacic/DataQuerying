package io.github.aleksadacic.dataquerying.internal.enums;

import io.github.aleksadacic.dataquerying.internal.deserializers.SortOrderDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = SortOrderDeserializer.class)
public enum SortOrder {
    ASC("ASC"),
    DESC("DESC");

    public final String value;

    SortOrder(String value) {
        this.value = value;
    }
}

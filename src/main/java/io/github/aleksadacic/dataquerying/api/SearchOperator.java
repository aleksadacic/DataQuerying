package io.github.aleksadacic.dataquerying.api;

import io.github.aleksadacic.dataquerying.internal.deserializers.SearchOperatorDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = SearchOperatorDeserializer.class)
public enum SearchOperator {
    EQ("EQ"),
    NOT_EQ("NOT_EQ"),
    GTE("GTE"),
    LTE("LTE"),
    GT("GT"),
    LT("LT"),
    IN("IN"),
    BETWEEN("BETWEEN"),
    LIKE("LIKE"),
    NOT_LIKE("NOT_LIKE");

    public final String operator;

    SearchOperator(String operator) {
        this.operator = operator;
    }
}

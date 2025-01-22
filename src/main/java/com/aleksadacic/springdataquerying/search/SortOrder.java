package com.aleksadacic.springdataquerying.search;

public enum SortOrder {
    ASC("ASC"),
    DESC("DESC");

    public final String value;

    SortOrder(String value) {
        this.value = value;
    }
}

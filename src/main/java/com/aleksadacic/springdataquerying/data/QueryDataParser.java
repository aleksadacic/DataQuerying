package com.aleksadacic.springdataquerying.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class QueryDataParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static QueryData parse(String json) throws IOException {
        return objectMapper.readValue(json, QueryData.class);
    }
}

package com.aleksadacic.springdataquerying.utils;

import com.aleksadacic.springdataquerying.exceptions.MappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class GenericConverter {
    private GenericConverter() {

    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> List<T> convertToList(List<Object[]> objectArrayList, Class<T> targetType) {
        return objectArrayList.stream()
                .map(objects -> mapToObject(objects, targetType))
                .toList();
    }

    private static <T> T mapToObject(Object[] objects, Class<T> targetType) {
        try {
            List<String> fieldNames = Arrays.stream(targetType.getDeclaredFields()).map(Field::getName).toList();
            // Build a JSON object from the Object[] and fieldNames
            String json = buildJsonFromObjects(objects, fieldNames);

            // Convert the JSON string to the target type
            return objectMapper.readValue(json, targetType);
        } catch (Exception e) {
            throw new MappingException("Error mapping Object[] to " + targetType.getSimpleName(), e);
        }
    }

    private static String buildJsonFromObjects(Object[] objects, List<String> fieldNames) {
        if (objects.length != fieldNames.size()) {
            throw new IllegalArgumentException("Object[] length and fieldNames length must match.");
        }

        StringBuilder jsonBuilder = new StringBuilder("{");
        for (int i = 0; i < objects.length; i++) {
            jsonBuilder.append("\"")
                    .append(fieldNames.get(i))
                    .append("\":")
                    .append(formatValueForJson(objects[i]));
            if (i < objects.length - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    private static String formatValueForJson(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + value.toString().replace("\"", "\\\"") + "\"";
        } else {
            return value.toString();
        }
    }
}
